package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.dto.PageResponse;
import com.my_band_lab.my_band_lab.entity.MusicGenre;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import com.my_band_lab.my_band_lab.entity.User;
import com.my_band_lab.my_band_lab.repository.MusicGroupRepository;
import com.my_band_lab.my_band_lab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MusicGroupServiceImpl implements MusicGroupService {

    @Autowired
    private MusicGroupRepository musicGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public MusicGroup createGroup(String name, String description, MusicGenre genre, Long founderId) throws Exception {
        System.out.println("=== CREATE GROUP DEBUG ===");
        System.out.println("1. Name: " + name);
        System.out.println("2. Genre: " + genre);
        System.out.println("3. founderId param: " + founderId);

        try {
            // Si no se proporciona founderId, usar el usuario autenticado
            User founder;
            if (founderId != null) {
                System.out.println("4. Using provided founderId");
                founder = userRepository.findById(founderId)
                        .orElseThrow(() -> new Exception("Founder user not found"));
            } else {
                System.out.println("4. Getting current user from SecurityContext");
                founder = getCurrentUser();
            }

            System.out.println("5. Founder ID: " + founder.getId());
            System.out.println("6. Founder Email: " + founder.getEmail());
            System.out.println("7. Founder Role: " + founder.getRole());

            // Verificar si ya existe un grupo con ese nombre
            System.out.println("8. Checking if group name exists...");
            if (musicGroupRepository.findByNameIgnoreCase(name).isPresent()) {
                System.out.println("9. ERROR: Group name already exists!");
                throw new Exception("Group name already exists");
            }

            // Verificar que el usuario tiene rol USER o ARTIST
            System.out.println("9. Checking role...");
            if (!founder.getRole().name().equals("USER") && !founder.getRole().name().equals("ARTIST")) {
                System.out.println("10. ERROR: Invalid role: " + founder.getRole());
                throw new Exception("Only users with USER or ARTIST role can create groups");
            }

            System.out.println("10. Creating group...");
            MusicGroup group = MusicGroup.builder()
                    .name(name)
                    .description(description)
                    .genre(genre)
                    .founder(founder)
                    .verified(false)
                    .build();

            // Añadir al fundador como miembro
            System.out.println("11. Adding founder as member...");
            group.getMembers().add(founder);

            System.out.println("12. Saving group...");
            MusicGroup saved = musicGroupRepository.save(group);
            System.out.println("13. Group saved with ID: " + saved.getId());
            System.out.println("14. Members count: " + saved.getMembers().size());

            return saved;

        } catch (Exception e) {
            System.out.println("=== ERROR ===");
            System.out.println("Exception: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    @Override
    @Transactional
    public MusicGroup addMember(Long groupId, Long userId) throws Exception {
        System.out.println("=== ADD MEMBER DEBUG ===");
        System.out.println("1. Group ID: " + groupId);
        System.out.println("2. User ID to add: " + userId);

        try {
            // Obtener el grupo
            MusicGroup group = musicGroupRepository.findById(groupId)
                    .orElseThrow(() -> new Exception("Group not found"));
            System.out.println("3. Group found: " + group.getName());
            System.out.println("4. Group founder ID: " + group.getFounder().getId());

            // Obtener el usuario autenticado
            User currentUser = getCurrentUser();
            System.out.println("5. Current user ID: " + currentUser.getId());
            System.out.println("6. Current user email: " + currentUser.getEmail());

            // Verificar que el usuario autenticado es el fundador del grupo
            if (!group.getFounder().getId().equals(currentUser.getId())) {
                System.out.println("7. ERROR: User is not the founder");
                throw new Exception("Only the group founder can add members");
            }
            System.out.println("7. User is the founder ✅");

            // Obtener el usuario a añadir
            User userToAdd = userRepository.findById(userId)
                    .orElseThrow(() -> new Exception("User not found"));
            System.out.println("8. User to add found: " + userToAdd.getEmail());

            // Verificar que no sea ya miembro
            if (group.getMembers().contains(userToAdd)) {
                System.out.println("9. ERROR: User is already a member");
                throw new Exception("User is already a member of this group");
            }
            System.out.println("9. User is not a member yet ✅");

            // Añadir miembro
            group.getMembers().add(userToAdd);
            System.out.println("10. Member added, saving group...");

            MusicGroup saved = musicGroupRepository.save(group);
            System.out.println("11. Group saved with ID: " + saved.getId());
            System.out.println("12. Members count: " + saved.getMembers().size());

            return saved;

        } catch (Exception e) {
            System.out.println("=== ERROR IN ADD MEMBER ===");
            System.out.println("Exception: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional
    public MusicGroup removeMember(Long groupId, Long userId) throws Exception {
        MusicGroup group = musicGroupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Group not found"));

        // Obtener el usuario autenticado
        User currentUser = getCurrentUser();

        // Verificar que el usuario autenticado es el fundador del grupo
        if (!group.getFounder().getId().equals(currentUser.getId())) {
            throw new Exception("Only the group founder can remove members");
        }

        // Obtener el usuario a remover
        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        if (!group.getMembers().contains(userToRemove)) {
            throw new Exception("User is not a member of this group");
        }

        // No permitir remover al fundador
        if (group.getFounder().getId().equals(userId)) {  // Cambiado leader a founder
            throw new Exception("Cannot remove the group founder");
        }

        group.getMembers().remove(userToRemove);
        return musicGroupRepository.save(group);
    }

    @Override
    public List<User> getGroupMembers(Long groupId) throws Exception {
        MusicGroup group = musicGroupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Group not found"));
        return group.getMembers();
    }

    @Override
    @Transactional
    public MusicGroup updateGroupGenre(Long groupId, MusicGenre genre) throws Exception {
        MusicGroup group = musicGroupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Group not found"));

        group.setGenre(genre);
        return musicGroupRepository.save(group);
    }

    @Override
    public List<MusicGroup> getGroupsByGenre(MusicGenre genre) throws Exception {
        return musicGroupRepository.findAll().stream()
                .filter(group -> group.getGenre() == genre)
                .collect(Collectors.toList());
    }

    @Override
    public List<MusicGroup> getAllGroups() throws Exception {
        List<MusicGroup> groups = musicGroupRepository.findAll();
        if (groups.isEmpty()) {
            throw new Exception("No music groups found");
        }
        return groups;
    }

    @Override
    public MusicGroup getGroupById(Long id) throws Exception {
        return musicGroupRepository.findById(id)
                .orElseThrow(() -> new Exception("Music group not found with id: " + id));
    }

    @Override
    public void deleteGroup(Long groupId) throws Exception {
        MusicGroup group = getGroupById(groupId);
        musicGroupRepository.delete(group);
    }
    @Override
    public PageResponse<MusicGroup> getAllGroupsPaginated(int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        Page<MusicGroup> groupPage = musicGroupRepository.findAll(pageable);

        return PageResponse.<MusicGroup>builder()
                .content(groupPage.getContent())
                .totalElements(groupPage.getTotalElements())
                .totalPages(groupPage.getTotalPages())
                .currentPage(groupPage.getNumber())
                .size(groupPage.getSize())
                .hasNext(groupPage.hasNext())
                .hasPrevious(groupPage.hasPrevious())
                .build();
    }
    @Override
    public PageResponse<MusicGroup> searchGroups(String query, int page, int size) throws Exception {
        Pageable pageable = PageRequest.of(page, size);
        Page<MusicGroup> groupPage = musicGroupRepository.searchByNameOrDescription(query, pageable);

        return PageResponse.<MusicGroup>builder()
                .content(groupPage.getContent())
                .totalElements(groupPage.getTotalElements())
                .totalPages(groupPage.getTotalPages())
                .currentPage(groupPage.getNumber())
                .size(groupPage.getSize())
                .hasNext(groupPage.hasNext())
                .hasPrevious(groupPage.hasPrevious())
                .build();
    }
    @Override
    public User getCurrentUser() throws Exception {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetails)) {
            throw new Exception("User not authenticated");
        }

        String email = ((UserDetails) principal).getUsername();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new Exception("User not found"));
    }
}