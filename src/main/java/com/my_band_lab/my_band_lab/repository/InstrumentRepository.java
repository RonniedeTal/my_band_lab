package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findByNameIgnoreCase(String name);

    List<Instrument> findByCategory(String category);

    List<Instrument> findAllByOrderByNameAsc();
}