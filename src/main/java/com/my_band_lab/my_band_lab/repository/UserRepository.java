package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
