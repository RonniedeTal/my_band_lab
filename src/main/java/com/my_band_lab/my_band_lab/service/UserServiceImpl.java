package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.dto.UserAdminResponse;
import com.my_band_lab.my_band_lab.dto.UserProfileResponse;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import com.my_band_lab.my_band_lab.entity.Role;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.repository.ArtistRepository;
import com.my_band_lab.my_band_lab.repository.MusicGroupRepository;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.my_band_lab.my_band_lab.dto.UpdateProfileRequest;
import com.my_band_lab.my_band_lab.dto.ArtistSummary;
import com.my_band_lab.my_band_lab.dto.GroupSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MusicGroupRepository musicGroupRepository;

    @Autowired
    private ArtistRepository artistRepository;

    //saveUser
    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findUserById(Long id) throws Exception {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new Exception("User not found");
        }
        return user.get();
    }

    @Override
    public User findUserByName(String name) throws Exception{
        Optional<User> user = userRepository.findByNameIgnoreCase(name);
        if (user.isEmpty()) {
            throw new Exception("User not found");
        }
        return user.get();
    }

    @Override
    public User findUserBySurname(String surname) throws Exception {
        Optional<User> user = userRepository.findBySurnameIgnoreCase(surname);
        if (user.isEmpty()) {
            throw new Exception("User not found");
        }
        return user.get();
    }

    @Override
    public User findUserByNameAndSurname(String name, String surname) throws Exception {
        Optional<User> user = userRepository.findByNameIgnoreCaseAndSurnameIgnoreCase(name, surname);
        if (user.isEmpty()) {
            throw new Exception("User not found with name: " + name + " and surname: " + surname);
        }
        return user.get();
    }

    @Override
    public User updateUser(Long id, User userDetails) throws Exception {
        // Buscar el usuario existente
        User existingUser = findUserById(id);

        // Actualizar solo los campos que vienen en la petición
        if (userDetails.getName() != null && !userDetails.getName().isEmpty()) {
            existingUser.setName(userDetails.getName());
        }

        if (userDetails.getSurname() != null && !userDetails.getSurname().isEmpty()) {
            existingUser.setSurname(userDetails.getSurname());
        }

        if (userDetails.getEmail() != null && !userDetails.getEmail().isEmpty()) {
            // Verificar si el nuevo email ya está en uso por otro usuario
            Optional<User> userWithEmail = userRepository.findByEmailIgnoreCase(userDetails.getEmail());
            if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
                throw new Exception("Email already in use: " + userDetails.getEmail());
            }
            existingUser.setEmail(userDetails.getEmail());
        }

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(userDetails.getPassword());
        }

        if (userDetails.getProfileImageUrl() != null) {
            existingUser.setProfileImageUrl(userDetails.getProfileImageUrl());
        }

        // El updatedAt se actualizará automáticamente con @PreUpdate
        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) throws Exception {
        User user = findUserById(id);
        userRepository.delete(user);
    }
    @Override
    public List<User> findAllUsers() throws Exception {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new Exception("No users found");
        }
        return users;
    }

    @Override
    public User getCurrentUser() throws Exception {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new Exception("User not found"));
        }

        throw new Exception("User not authenticated");
    }

    @Override
    public UserProfileResponse getCurrentUserProfile() throws Exception {
        User user = getCurrentUser();

        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request) throws Exception {
        User user = getCurrentUser();

        if (request.getName() != null && !request.getName().isEmpty()) {
            user.setName(request.getName());
        }

        if (request.getSurname() != null && !request.getSurname().isEmpty()) {
            user.setSurname(request.getSurname());
        }

        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        User updatedUser = userRepository.save(user);

        return UserProfileResponse.builder()
                .id(updatedUser.getId())
                .name(updatedUser.getName())
                .surname(updatedUser.getSurname())
                .email(updatedUser.getEmail())
                .role(updatedUser.getRole().name())
                .createdAt(updatedUser.getCreatedAt())
                .build();
    }

    @Override
    public List<UserAdminResponse> getAllUsersForAdmin() throws Exception {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new Exception("No users found");
        }
        return users.stream()
                .map(this::convertToAdminResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<UserAdminResponse> getAllUsersForAdminPaginated(int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);

        List<UserAdminResponse> content = userPage.getContent().stream()
                .map(this::convertToAdminResponse)
                .collect(Collectors.toList());

        return PageResponse.<UserAdminResponse>builder()
                .content(content)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .currentPage(userPage.getNumber())
                .size(userPage.getSize())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }

    @Override
    public List<UserAdminResponse> getUsersByRoleForAdmin(String role) throws Exception {
        Role userRole;
        try {
            userRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid role. Valid roles: USER, ARTIST, ADMIN");
        }

        List<User> users = userRepository.findByRole(userRole);
        if (users.isEmpty()) {
            throw new Exception("No users found with role: " + role);
        }
        return users.stream()
                .map(this::convertToAdminResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<UserAdminResponse> getUsersByRoleForAdminPaginated(String role, int page, int size) throws Exception {
        Role userRole;
        try {
            userRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid role. Valid roles: USER, ARTIST, ADMIN");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findByRole(userRole, pageable);

        List<UserAdminResponse> content = userPage.getContent().stream()
                .map(this::convertToAdminResponse)
                .collect(Collectors.toList());

        return PageResponse.<UserAdminResponse>builder()
                .content(content)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .currentPage(userPage.getNumber())
                .size(userPage.getSize())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }

    private UserAdminResponse convertToAdminResponse(User user) {
        // Obtener información del artista si existe
        ArtistSummary artistSummary = null;
        if (user.getArtist() != null) {
            artistSummary = ArtistSummary.builder()
                    .id(user.getArtist().getId())
                    .stageName(user.getArtist().getStageName())
                    .verified(user.getArtist().isVerified())
                    .build();
        }

        // Obtener información de grupos
        List<GroupSummary> groupSummaries = new ArrayList<>();
        if (user.getMusicGroups() != null) {
            for (var group : user.getMusicGroups()) {
                String roleInGroup = group.getFounder() != null &&
                        group.getFounder().getId().equals(user.getId()) ? "FOUNDER" : "MEMBER";
                groupSummaries.add(GroupSummary.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .role(roleInGroup)
                        .build());
            }
        }

        return UserAdminResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .role(user.getRole().name())
                .profileImageUrl(user.getProfileImageUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .artist(artistSummary)
                .groups(groupSummaries)
                .build();
    }
    @Override
    public UserAdminResponse getUserByIdForAdmin(Long id) throws Exception {
        User user = findUserById(id);
        return convertToAdminResponse(user);
    }
    @Override
    @Transactional
    public UserAdminResponse changeUserRole(Long userId, String newRole, Long currentAdminId) throws Exception {
        // 1. Validar que el rol sea válido
        Role targetRole;
        try {
            targetRole = Role.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid role. Valid roles: USER, ARTIST, ADMIN");
        }

        // 2. Buscar el usuario a modificar
        User user = findUserById(userId);

        // 3. No permitir cambiar el propio rol
        if (userId.equals(currentAdminId)) {
            throw new Exception("You cannot change your own role");
        }

        // 4. Actualizar el rol
        user.setRole(targetRole);
        User updatedUser = userRepository.save(user);

        // 5. Devolver el usuario actualizado
        return convertToAdminResponse(updatedUser);
    }
    @Override
    @Transactional
    public void deleteUserByAdmin(Long userId, Long currentAdminId) throws Exception {
        // 1. No permitir eliminar el propio usuario
        if (userId.equals(currentAdminId)) {
            throw new Exception("You cannot delete your own account");
        }

        // 2. Buscar el usuario a eliminar
        User user = findUserById(userId);

        // 3. Eliminar relaciones del usuario con grupos donde es miembro
        if (user.getMusicGroups() != null && !user.getMusicGroups().isEmpty()) {
            for (MusicGroup group : user.getMusicGroups()) {
                // Si el usuario es fundador, transferir fundador o advertir
                if (group.getFounder() != null && group.getFounder().getId().equals(userId)) {
                    // Buscar otro miembro para transferir fundador
                    List<User> members = group.getMembers().stream()
                            .filter(m -> !m.getId().equals(userId))
                            .collect(Collectors.toList());

                    if (members.isEmpty()) {
                        // Si no hay otros miembros, eliminar el grupo
                        musicGroupRepository.delete(group);
                    } else {
                        // Transferir fundador al primer miembro disponible
                        User newFounder = members.get(0);
                        group.setFounder(newFounder);
                        // Eliminar al usuario de los miembros
                        group.getMembers().remove(user);
                        musicGroupRepository.save(group);
                    }
                } else {
                    // Si no es fundador, solo remover de miembros
                    group.getMembers().remove(user);
                    musicGroupRepository.save(group);
                }
            }
        }

        // 4. Eliminar el artista si existe
        if (user.getArtist() != null) {
            artistRepository.delete(user.getArtist());
        }

        // 5. Eliminar el usuario
        userRepository.delete(user);
    }
    @Override
    public User updateProfileImage(String profileImageUrl) throws Exception {
        User currentUser = getCurrentUser();
        currentUser.setProfileImageUrl(profileImageUrl);
        return userRepository.save(currentUser);
    }
    @Override
    public User findUserByEmail(String email) throws Exception {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new Exception("User not found with email: " + email));
    }

}
