package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.Instrument;
import com.my_band_lab.my_band_lab.repository.InstrumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InstrumentServiceImpl implements InstrumentService {

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Override
    public List<Instrument> getAllInstruments() {
        return instrumentRepository.findAllByOrderByNameAsc();
    }

    @Override
    public List<Instrument> getInstrumentsByCategory(String category) {
        return instrumentRepository.findByCategory(category);
    }
}