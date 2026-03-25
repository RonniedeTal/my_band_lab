package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.Instrument;
import java.util.List;

public interface InstrumentService {
    List<Instrument> getAllInstruments();
    List<Instrument> getInstrumentsByCategory(String category);
}