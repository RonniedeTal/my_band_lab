package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.entity.Instrument;
import com.my_band_lab.my_band_lab.service.InstrumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    @Autowired
    private InstrumentService instrumentService;

    @GetMapping
    public ResponseEntity<List<Instrument>> getAllInstruments() {
        List<Instrument> instruments = instrumentService.getAllInstruments();
        return ResponseEntity.ok(instruments);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Instrument>> getInstrumentsByCategory(@PathVariable String category) {
        List<Instrument> instruments = instrumentService.getInstrumentsByCategory(category);
        return ResponseEntity.ok(instruments);
    }
}