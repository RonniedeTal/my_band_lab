package com.my_band_lab.my_band_lab.controller;

import com.my_band_lab.my_band_lab.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/countries")
@CrossOrigin(origins = "http://localhost:5173")
public class CountryController {

    @Autowired
    private CountryService countryService;

    @GetMapping
    public List<Map<String, Object>> getCountries() {
        return countryService.getAllCountries();
    }

    @GetMapping("/{countryName}/cities")
    public List<String> getCitiesByCountry(@PathVariable String countryName) {
        return countryService.getCitiesByCountry(countryName);
    }
}