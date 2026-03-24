package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.MusicGenre;
import com.my_band_lab.my_band_lab.entity.MusicGroup;
import com.my_band_lab.my_band_lab.entity.User;

import java.util.List;

public interface MusicGroupService {
    MusicGroup createGroup(String name, String description, MusicGenre genre, Long founderId) throws Exception;
    MusicGroup addMember(Long groupId, Long userId) throws Exception;
    MusicGroup removeMember(Long groupId, Long userId) throws Exception;
    List<User> getGroupMembers(Long groupId) throws Exception;
    MusicGroup updateGroupGenre(Long groupId, MusicGenre genre) throws Exception;
    List<MusicGroup> getGroupsByGenre(MusicGenre genre) throws Exception;
    List<MusicGroup> getAllGroups() throws Exception;
    MusicGroup getGroupById(Long id) throws Exception;
    void deleteGroup(Long groupId) throws Exception;
}