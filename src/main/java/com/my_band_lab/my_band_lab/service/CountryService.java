package com.my_band_lab.my_band_lab.service;

import java.util.List;
import java.util.Map;

public interface CountryService {
    List<Map<String, Object>> getAllCountries();
    List<String> getCitiesByCountry(String countryName);
}