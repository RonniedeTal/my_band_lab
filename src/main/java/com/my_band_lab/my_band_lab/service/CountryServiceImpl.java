package com.my_band_lab.my_band_lab.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CountryServiceImpl implements CountryService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String COUNTRIES_API_URL = "https://restcountries.com/v3.1/all?fields=name,cca2,flags";
    private static final String CITIES_API_URL = "https://nominatim.openstreetmap.org/search";

    @Override
    public List<Map<String, Object>> getAllCountries() {
        try {
            String response = restTemplate.getForObject(COUNTRIES_API_URL, String.class);
            JsonNode countries = objectMapper.readTree(response);

            List<Map<String, Object>> countryList = new ArrayList<>();

            for (JsonNode country : countries) {
                String name = country.path("name").path("common").asText();
                String code = country.path("cca2").asText();
                String flag = country.path("flags").path("svg").asText();

                if (!name.isEmpty()) {
                    Map<String, Object> countryMap = new HashMap<>();
                    countryMap.put("name", name);
                    countryMap.put("code", code);
                    countryMap.put("flag", flag);
                    countryList.add(countryMap);
                }
            }

            return countryList.stream()
                    .sorted((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name")))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultCountries();
        }
    }

    @Override
    public List<String> getCitiesByCountry(String countryName) {
        if (countryName == null || countryName.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            String decodedCountryName = java.net.URLDecoder.decode(countryName, StandardCharsets.UTF_8.name());

            // Mapear nombre del país al formato que entiende Nominatim (español)
            String searchName = mapToSpanishName(decodedCountryName);

            String encodedQuery = URLEncoder.encode(searchName, StandardCharsets.UTF_8.toString());
            String url = CITIES_API_URL + "?country=" + encodedQuery +
                    "&format=json&limit=100&addressdetails=1" +
                    "&featureclass=P" +
                    "&dedupe=1";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "MyBandLab/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getBody() != null && !response.getBody().isEmpty()) {
                JsonNode json = objectMapper.readTree(response.getBody());
                Set<String> cities = new LinkedHashSet<>();

                if (json.isArray()) {
                    for (JsonNode item : json) {
                        String displayName = item.path("display_name").asText();
                        String cityName = extractCityName(displayName);
                        if (cityName != null && !cityName.isEmpty() && isValidCity(cityName)) {
                            cities.add(cityName);
                        }
                    }
                }

                if (!cities.isEmpty()) {
                    return cities.stream()
                            .sorted()
                            .limit(50)
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return getDefaultCitiesByCountry(countryName);
    }

    /**
     * Mapea nombres de países en inglés a español para la búsqueda en Nominatim
     */
    private String mapToSpanishName(String countryName) {
        Map<String, String> nameMapping = new HashMap<>();
        nameMapping.put("Spain", "España");
        nameMapping.put("Mexico", "México");
        nameMapping.put("Argentina", "Argentina");
        nameMapping.put("Colombia", "Colombia");
        nameMapping.put("Chile", "Chile");
        nameMapping.put("Peru", "Perú");
        nameMapping.put("Venezuela", "Venezuela");
        nameMapping.put("United States", "Estados Unidos");
        nameMapping.put("United States of America", "Estados Unidos");
        nameMapping.put("Uruguay", "Uruguay");
        nameMapping.put("Paraguay", "Paraguay");
        nameMapping.put("Bolivia", "Bolivia");
        nameMapping.put("Ecuador", "Ecuador");
        nameMapping.put("Costa Rica", "Costa Rica");
        nameMapping.put("Panama", "Panamá");
        nameMapping.put("Brazil", "Brasil");
        nameMapping.put("Canada", "Canadá");
        nameMapping.put("Germany", "Alemania");
        nameMapping.put("France", "Francia");
        nameMapping.put("Italy", "Italia");
        nameMapping.put("United Kingdom", "Reino Unido");
        nameMapping.put("Portugal", "Portugal");
        nameMapping.put("Netherlands", "Países Bajos");
        nameMapping.put("Belgium", "Bélgica");
        nameMapping.put("Switzerland", "Suiza");
        nameMapping.put("Sweden", "Suecia");
        nameMapping.put("Norway", "Noruega");
        nameMapping.put("Denmark", "Dinamarca");
        nameMapping.put("Finland", "Finlandia");
        nameMapping.put("Ireland", "Irlanda");
        nameMapping.put("Austria", "Austria");
        nameMapping.put("Greece", "Grecia");
        nameMapping.put("Turkey", "Turquía");
        nameMapping.put("Russia", "Rusia");
        nameMapping.put("China", "China");
        nameMapping.put("Japan", "Japón");
        nameMapping.put("South Korea", "Corea del Sur");
        nameMapping.put("India", "India");
        nameMapping.put("Australia", "Australia");
        nameMapping.put("New Zealand", "Nueva Zelanda");

        return nameMapping.getOrDefault(countryName, countryName);
    }

    private String extractCityName(String displayName) {
        if (displayName == null) return null;
        String[] parts = displayName.split(",");
        if (parts.length > 0) {
            String candidate = parts[0].trim();
            // Limpiar nombres extraños
            candidate = candidate.replaceAll("\\[.*?\\]", "").trim();
            candidate = candidate.replaceAll("\\(.*?\\)", "").trim();
            return candidate;
        }
        return null;
    }

    private boolean isValidCity(String cityName) {
        if (cityName == null || cityName.isEmpty()) return false;

        String lowerCity = cityName.toLowerCase();

        // Filtrar nombres que no son ciudades
        String[] invalidKeywords = {
                "locker", "room", "poblado", "flagstone", "province", "region",
                "state", "country", "district", "parish", "municipality",
                "unincorporated", "census", "designated", "place", "locality",
                "hamlet", "village", "township", "county", "department"
        };

        for (String keyword : invalidKeywords) {
            if (lowerCity.contains(keyword)) {
                return false;
            }
        }

        // Ciudad no debe ser muy larga ni tener caracteres raros
        if (cityName.length() > 40) return false;
        if (cityName.contains("(") || cityName.contains(")")) return false;
        if (cityName.contains("/") || cityName.contains("\\")) return false;

        return true;
    }

    private List<Map<String, Object>> getDefaultCountries() {
        List<Map<String, Object>> defaults = new ArrayList<>();

        String[][] defaultCountries = {
                {"España", "ES"}, {"México", "MX"}, {"Argentina", "AR"}, {"Colombia", "CO"},
                {"Chile", "CL"}, {"Perú", "PE"}, {"Venezuela", "VE"}, {"Estados Unidos", "US"},
                {"Uruguay", "UY"}, {"Paraguay", "PY"}, {"Bolivia", "BO"}, {"Ecuador", "EC"},
                {"Costa Rica", "CR"}, {"Panamá", "PA"}, {"Brasil", "BR"}, {"Canadá", "CA"},
                {"Alemania", "DE"}, {"Francia", "FR"}, {"Italia", "IT"}, {"Reino Unido", "GB"},
                {"Portugal", "PT"}, {"Países Bajos", "NL"}, {"Bélgica", "BE"}, {"Suiza", "CH"},
                {"Suecia", "SE"}, {"Noruega", "NO"}, {"Dinamarca", "DK"}, {"Finlandia", "FI"}
        };

        for (String[] country : defaultCountries) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", country[0]);
            map.put("code", country[1]);
            map.put("flag", "");
            defaults.add(map);
        }

        return defaults;
    }

    private List<String> getDefaultCitiesByCountry(String countryName) {
        String decodedName = countryName;
        try {
            decodedName = java.net.URLDecoder.decode(countryName, StandardCharsets.UTF_8.name());
        } catch (Exception e) {}

        Map<String, List<String>> defaultCities = new HashMap<>();

        // España - ciudades principales
        defaultCities.put("España", Arrays.asList(
                "Madrid", "Barcelona", "Valencia", "Sevilla", "Zaragoza", "Málaga", "Murcia",
                "Palma de Mallorca", "Las Palmas", "Bilbao", "Alicante", "Córdoba", "Valladolid",
                "Vigo", "Gijón", "Granada", "A Coruña", "Vitoria", "Santa Cruz de Tenerife",
                "Badalona", "Oviedo", "Elche", "Cartagena", "Terrassa", "Jerez", "Sabadell"
        ));

        // México
        defaultCities.put("México", Arrays.asList(
                "Ciudad de México", "Guadalajara", "Monterrey", "Puebla", "Toluca", "Tijuana",
                "León", "Querétaro", "San Luis Potosí", "Mérida", "Aguascalientes", "Hermosillo",
                "Saltillo", "Morelia", "Cancún", "Mexicali", "Culiacán", "Cuernavaca"
        ));

        // Argentina
        defaultCities.put("Argentina", Arrays.asList(
                "Buenos Aires", "Córdoba", "Rosario", "Mendoza", "La Plata", "San Miguel de Tucumán",
                "Mar del Plata", "Salta", "Santa Fe", "San Juan", "Resistencia", "Neuquén",
                "Posadas", "Bahía Blanca", "Paraná", "Formosa", "San Luis", "La Rioja"
        ));

        // Colombia
        defaultCities.put("Colombia", Arrays.asList(
                "Bogotá", "Medellín", "Cali", "Barranquilla", "Cartagena", "Cúcuta", "Bucaramanga",
                "Pereira", "Santa Marta", "Ibagué", "Manizales", "Villavicencio", "Montería",
                "Sincelejo", "Neiva", "Pasto", "Armenia", "Popayán"
        ));

        // Chile
        defaultCities.put("Chile", Arrays.asList(
                "Santiago", "Valparaíso", "Concepción", "La Serena", "Antofagasta", "Viña del Mar",
                "Temuco", "Rancagua", "Iquique", "Puerto Montt", "Talca", "Arica", "Chillán",
                "Calama", "Los Ángeles", "Valdivia", "Punta Arenas"
        ));

        // Perú
        defaultCities.put("Perú", Arrays.asList(
                "Lima", "Arequipa", "Trujillo", "Cusco", "Piura", "Chiclayo", "Huancayo", "Iquitos",
                "Pucallpa", "Tacna", "Juliaca", "Sullana", "Cajamarca", "Huaraz", "Puno"
        ));

        // Estados Unidos
        defaultCities.put("Estados Unidos", Arrays.asList(
                "Nueva York", "Los Ángeles", "Chicago", "Houston", "Phoenix", "Filadelfia",
                "San Antonio", "San Diego", "Dallas", "San José", "Austin", "Jacksonville",
                "Fort Worth", "Columbus", "Indianápolis", "Charlotte", "San Francisco",
                "Seattle", "Denver", "Washington DC", "Boston", "Miami", "Atlanta", "Las Vegas"
        ));

        // Buscar por nombre alternativo (inglés)
        String lowerName = decodedName.toLowerCase();
        for (Map.Entry<String, List<String>> entry : defaultCities.entrySet()) {
            if (entry.getKey().toLowerCase().equals(lowerName)) {
                return entry.getValue();
            }
        }

        return new ArrayList<>();
    }
}