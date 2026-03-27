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

        // Si no se proporciona founderId, usar el usuario autenticado
        User founder;
        if (founderId != null) {
            founder = userRepository.findById(founderId)
                    .orElseThrow(() -> new Exception("Founder user not found"));
        } else {
            founder = getCurrentUser();
        }
        // Verificar si ya existe un grupo con ese nombre
        if (musicGroupRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new Exception("Group name already exists");
        }

//        User founder = userRepository.findById(founderId)
//                .orElseThrow(() -> new Exception("Founder user not found"));
        // Verificar que el usuario tiene rol USER o ARTIST
        if (!founder.getRole().name().equals("USER") && !founder.getRole().name().equals("ARTIST")) {
            throw new Exception("Only users with USER or ARTIST role can create groups");
        }

        MusicGroup group = MusicGroup.builder()
                .name(name)
                .description(description)
                .genre(genre)
                .founder(founder)  // Cambiado leader a founder
                .verified(false)

                .build();

        // Añadir al líder como miembro
        group.getMembers().add(founder);

        return musicGroupRepository.save(group);
    }

    @Override
    @Transactional
    public MusicGroup addMember(Long groupId, Long userId) throws Exception {
        MusicGroup group = musicGroupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Group not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        if (group.getMembers().contains(user)) {
            throw new Exception("User is already a member of this group");
        }

        group.getMembers().add(user);
        return musicGroupRepository.save(group);
    }

    @Override
    @Transactional
    public MusicGroup removeMember(Long groupId, Long userId) throws Exception {
        MusicGroup group = musicGroupRepository.findById(groupId)
                .orElseThrow(() -> new Exception("Group not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        if (!group.getMembers().contains(user)) {
            throw new Exception("User is not a member of this group");
        }

        // No permitir remover al fundador
        if (group.getFounder().getId().equals(userId)) {  // Cambiado leader a founder
            throw new Exception("Cannot remove the group founder");
        }

        group.getMembers().remove(user);
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