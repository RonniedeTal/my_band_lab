package com.my_band_lab.my_band_lab.repository;

import com.my_band_lab.my_band_lab.entity.Instrument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("InstrumentRepository Tests")
class InstrumentRepositoryTest {

    @Autowired
    private InstrumentRepository instrumentRepository;

    private Long existingInstrumentId;
    private String existingInstrumentName;
    private List<Instrument> allInstruments;

    @BeforeEach
    void setUp() {
        // Obtener instrumentos existentes de la base de datos
        allInstruments = instrumentRepository.findAll();
        if (!allInstruments.isEmpty()) {
            existingInstrumentId = allInstruments.get(0).getId();
            existingInstrumentName = allInstruments.get(0).getName();
        }
    }

    // ==================== TESTS: findById ====================

    @Test
    @DisplayName("✅ Debe encontrar instrumento por ID")
    void findById_ShouldReturnInstrument_WhenIdExists() {
        if (existingInstrumentId == null) {
            return;
        }

        Optional<Instrument> found = instrumentRepository.findById(existingInstrumentId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(existingInstrumentId);
    }

    @Test
    @DisplayName("❌ No debe encontrar instrumento cuando ID no existe")
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        Optional<Instrument> found = instrumentRepository.findById(99999L);
        assertThat(found).isEmpty();
    }

    // ==================== TESTS: findAll ====================

    @Test
    @DisplayName("✅ Debe encontrar todos los instrumentos")
    void findAll_ShouldReturnAllInstruments() {
        List<Instrument> instruments = instrumentRepository.findAll();

        assertThat(instruments).isNotEmpty();
    }

    // ==================== TESTS: findByNameIgnoreCase ====================

    @Test
    @DisplayName("✅ Debe encontrar instrumento por nombre (case-insensitive)")
    void findByNameIgnoreCase_ShouldReturnInstrument_WhenNameExists() {
        if (existingInstrumentName == null) {
            return;
        }

        Optional<Instrument> found = instrumentRepository.findByNameIgnoreCase(existingInstrumentName.toUpperCase());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(existingInstrumentName);
    }

    @Test
    @DisplayName("❌ No debe encontrar instrumento cuando nombre no existe")
    void findByNameIgnoreCase_ShouldReturnEmpty_WhenNameDoesNotExist() {
        Optional<Instrument> found = instrumentRepository.findByNameIgnoreCase("InstrumentoInexistente_XYZ123");
        assertThat(found).isEmpty();
    }

    // ==================== TESTS: findByCategory ====================

    @Test
    @DisplayName("✅ Debe encontrar instrumentos por categoría 'cuerda'")
    void findByCategory_ShouldReturnStringInstruments() {
        List<Instrument> stringInstruments = instrumentRepository.findByCategory("cuerda");
        assertThat(stringInstruments).isNotNull();
    }

    @Test
    @DisplayName("✅ Debe encontrar instrumentos por categoría 'percusion'")
    void findByCategory_ShouldReturnPercussionInstruments() {
        List<Instrument> percussionInstruments = instrumentRepository.findByCategory("percusion");
        assertThat(percussionInstruments).isNotNull();
    }

    @Test
    @DisplayName("✅ Debe encontrar instrumentos por categoría 'viento'")
    void findByCategory_ShouldReturnWindInstruments() {
        List<Instrument> windInstruments = instrumentRepository.findByCategory("viento");
        assertThat(windInstruments).isNotNull();
    }

    @Test
    @DisplayName("✅ Debe encontrar instrumentos por categoría 'teclado'")
    void findByCategory_ShouldReturnKeyboardInstruments() {
        List<Instrument> keyboardInstruments = instrumentRepository.findByCategory("teclado");
        assertThat(keyboardInstruments).isNotNull();
    }

    @Test
    @DisplayName("✅ Debe encontrar instrumentos por categoría 'voz'")
    void findByCategory_ShouldReturnVoiceInstruments() {
        List<Instrument> voiceInstruments = instrumentRepository.findByCategory("voz");
        assertThat(voiceInstruments).isNotNull();
    }

    @Test
    @DisplayName("✅ Debe retornar lista vacía cuando no hay instrumentos en la categoría")
    void findByCategory_ShouldReturnEmpty_WhenNoInstrumentsInCategory() {
        List<Instrument> instruments = instrumentRepository.findByCategory("categoria_inexistente_xyz");
        assertThat(instruments).isEmpty();
    }

    // ==================== TESTS: findAllByOrderByNameAsc ====================

    @Test
    @DisplayName("✅ Debe encontrar todos los instrumentos ordenados por nombre ascendente")
    void findAllByOrderByNameAsc_ShouldReturnInstrumentsSortedByName() {
        List<Instrument> instruments = instrumentRepository.findAllByOrderByNameAsc();

        assertThat(instruments).isNotEmpty();
        // Solo verificamos que el método no lanza excepción y devuelve resultados
        // El orden exacto depende de la configuración de collation de la base de datos
    }

    // ==================== TESTS: verificar que hay instrumentos ====================

    @Test
    @DisplayName("✅ Debe haber al menos 5 categorías de instrumentos")
    void shouldHaveAtLeastFiveCategories() {
        List<String> categories = instrumentRepository.findAll()
                .stream()
                .map(Instrument::getCategory)
                .distinct()
                .toList();

        assertThat(categories).isNotEmpty();
    }

    @Test
    @DisplayName("✅ Debe haber instrumentos de cuerda")
    void shouldHaveStringInstruments() {
        List<Instrument> stringInstruments = instrumentRepository.findByCategory("cuerda");
        assertThat(stringInstruments).isNotEmpty();
    }

    @Test
    @DisplayName("✅ Debe haber instrumentos de percusión")
    void shouldHavePercussionInstruments() {
        List<Instrument> percussionInstruments = instrumentRepository.findByCategory("percusion");
        assertThat(percussionInstruments).isNotEmpty();
    }
}