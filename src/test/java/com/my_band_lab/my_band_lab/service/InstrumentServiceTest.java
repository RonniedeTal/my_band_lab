package com.my_band_lab.my_band_lab.service;

import com.my_band_lab.my_band_lab.entity.Instrument;
import com.my_band_lab.my_band_lab.repository.InstrumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InstrumentService Tests")
class InstrumentServiceTest {

    @Mock
    private InstrumentRepository instrumentRepository;

    @InjectMocks
    private InstrumentServiceImpl instrumentService;

    private Instrument guitar;
    private Instrument piano;
    private Instrument drums;

    @BeforeEach
    void setUp() {
        guitar = Instrument.builder()
                .id(1L)
                .name("Guitarra Eléctrica")
                .category("cuerda")
                .icon("guitar.png")
                .build();

        piano = Instrument.builder()
                .id(2L)
                .name("Piano")
                .category("teclado")
                .icon("piano.png")
                .build();

        drums = Instrument.builder()
                .id(3L)
                .name("Batería")
                .category("percusion")
                .icon("drums.png")
                .build();
    }

    // ==================== TESTS: getAllInstruments ====================

    @Test
    @DisplayName("✅ Debe retornar todos los instrumentos ordenados por nombre")
    void getAllInstruments_ShouldReturnAllInstrumentsSortedByName() {
        List<Instrument> expectedInstruments = Arrays.asList(guitar, piano, drums);
        when(instrumentRepository.findAllByOrderByNameAsc()).thenReturn(expectedInstruments);

        List<Instrument> result = instrumentService.getAllInstruments();

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(guitar, piano, drums);
        verify(instrumentRepository, times(1)).findAllByOrderByNameAsc();
    }

    @Test
    @DisplayName("✅ Debe retornar lista vacía cuando no hay instrumentos")
    void getAllInstruments_ShouldReturnEmptyList_WhenNoInstruments() {
        when(instrumentRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

        List<Instrument> result = instrumentService.getAllInstruments();

        assertThat(result).isEmpty();
        verify(instrumentRepository, times(1)).findAllByOrderByNameAsc();
    }

    // ==================== TESTS: getInstrumentsByCategory ====================

    @Test
    @DisplayName("✅ Debe retornar instrumentos por categoría 'cuerda'")
    void getInstrumentsByCategory_ShouldReturnStringInstruments() {
        List<Instrument> expectedInstruments = Arrays.asList(guitar);
        when(instrumentRepository.findByCategory("cuerda")).thenReturn(expectedInstruments);

        List<Instrument> result = instrumentService.getInstrumentsByCategory("cuerda");

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Guitarra Eléctrica");
        verify(instrumentRepository, times(1)).findByCategory("cuerda");
    }

    @Test
    @DisplayName("✅ Debe retornar instrumentos por categoría 'teclado'")
    void getInstrumentsByCategory_ShouldReturnKeyboardInstruments() {
        List<Instrument> expectedInstruments = Arrays.asList(piano);
        when(instrumentRepository.findByCategory("teclado")).thenReturn(expectedInstruments);

        List<Instrument> result = instrumentService.getInstrumentsByCategory("teclado");

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Piano");
        verify(instrumentRepository, times(1)).findByCategory("teclado");
    }

    @Test
    @DisplayName("✅ Debe retornar instrumentos por categoría 'percusion'")
    void getInstrumentsByCategory_ShouldReturnPercussionInstruments() {
        List<Instrument> expectedInstruments = Arrays.asList(drums);
        when(instrumentRepository.findByCategory("percusion")).thenReturn(expectedInstruments);

        List<Instrument> result = instrumentService.getInstrumentsByCategory("percusion");

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Batería");
        verify(instrumentRepository, times(1)).findByCategory("percusion");
    }

    @Test
    @DisplayName("✅ Debe retornar lista vacía cuando la categoría no tiene instrumentos")
    void getInstrumentsByCategory_ShouldReturnEmptyList_WhenCategoryHasNoInstruments() {
        when(instrumentRepository.findByCategory("categoria_vacia")).thenReturn(List.of());

        List<Instrument> result = instrumentService.getInstrumentsByCategory("categoria_vacia");

        assertThat(result).isEmpty();
        verify(instrumentRepository, times(1)).findByCategory("categoria_vacia");
    }

    @Test
    @DisplayName("✅ Debe manejar category null (el servicio lo pasa al repositorio)")
    void getInstrumentsByCategory_ShouldHandleNullCategory() {
        when(instrumentRepository.findByCategory(null)).thenReturn(List.of());

        List<Instrument> result = instrumentService.getInstrumentsByCategory(null);

        assertThat(result).isEmpty();
        verify(instrumentRepository, times(1)).findByCategory(null);
    }

    @Test
    @DisplayName("✅ Debe manejar category vacío")
    void getInstrumentsByCategory_ShouldHandleEmptyCategory() {
        when(instrumentRepository.findByCategory("")).thenReturn(List.of());

        List<Instrument> result = instrumentService.getInstrumentsByCategory("");

        assertThat(result).isEmpty();
        verify(instrumentRepository, times(1)).findByCategory("");
    }

    @Test
    @DisplayName("✅ Debe manejar category con espacios")
    void getInstrumentsByCategory_ShouldHandleBlankCategory() {
        when(instrumentRepository.findByCategory("   ")).thenReturn(List.of());

        List<Instrument> result = instrumentService.getInstrumentsByCategory("   ");

        assertThat(result).isEmpty();
        verify(instrumentRepository, times(1)).findByCategory("   ");
    }
}