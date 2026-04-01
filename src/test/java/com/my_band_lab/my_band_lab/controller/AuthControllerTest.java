package com.my_band_lab.my_band_lab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my_band_lab.my_band_lab.dto.LoginRequest;
import com.my_band_lab.my_band_lab.dto.LoginResponse;
import com.my_band_lab.my_band_lab.dto.RegisterRequest;
import com.my_band_lab.my_band_lab.dto.RegisterResponse;
import com.my_band_lab.my_band_lab.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private RegisterResponse registerResponse;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        // Register Request
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setName("Test");
        validRegisterRequest.setSurname("User");
        validRegisterRequest.setEmail("test@example.com");
        validRegisterRequest.setPassword("123456");

        // Login Request
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("123456");

        // Register Response
        registerResponse = new RegisterResponse();
        registerResponse.setId(1L);
        registerResponse.setName("Test");
        registerResponse.setSurname("User");
        registerResponse.setEmail("test@example.com");
        registerResponse.setRole("USER");
        registerResponse.setCreatedAt(java.time.LocalDateTime.now());

        // Login Response
        loginResponse = new LoginResponse();
        loginResponse.setToken("eyJhbGciOiJIUzI1NiJ9.test-token");
        loginResponse.setType("Bearer");
        loginResponse.setId(1L);
        loginResponse.setName("Test");
        loginResponse.setSurname("User");
        loginResponse.setEmail("test@example.com");
        loginResponse.setRole("USER");
    }

    // ==================== TESTS: POST /auth/register ====================

    @Nested
    @DisplayName("POST /auth/register Tests")
    class RegisterTests {

        @Test
        @DisplayName("✅ Debe registrar usuario exitosamente")
        void register_ShouldReturnRegisterResponse_WhenValid() throws Exception {
            when(authService.register(any(RegisterRequest.class))).thenReturn(registerResponse);

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Test"))
                    .andExpect(jsonPath("$.surname").value("User"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.role").value("USER"));

            verify(authService, times(1)).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName("❌ Debe retornar 400 cuando email es inválido")
        void register_ShouldReturnBadRequest_WhenInvalidEmail() throws Exception {
            RegisterRequest invalidRequest = new RegisterRequest();
            invalidRequest.setName("Test");
            invalidRequest.setSurname("User");
            invalidRequest.setEmail("invalid-email");
            invalidRequest.setPassword("123456");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("❌ Debe retornar 400 cuando password es muy corto")
        void register_ShouldReturnBadRequest_WhenPasswordTooShort() throws Exception {
            RegisterRequest invalidRequest = new RegisterRequest();
            invalidRequest.setName("Test");
            invalidRequest.setSurname("User");
            invalidRequest.setEmail("test@example.com");
            invalidRequest.setPassword("12345");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("❌ Debe retornar 400 cuando nombre es muy corto")
        void register_ShouldReturnBadRequest_WhenNameTooShort() throws Exception {
            RegisterRequest invalidRequest = new RegisterRequest();
            invalidRequest.setName("Te");
            invalidRequest.setSurname("User");
            invalidRequest.setEmail("test@example.com");
            invalidRequest.setPassword("123456");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("❌ Debe retornar 400 cuando apellido es muy corto")
        void register_ShouldReturnBadRequest_WhenSurnameTooShort() throws Exception {
            RegisterRequest invalidRequest = new RegisterRequest();
            invalidRequest.setName("Test");
            invalidRequest.setSurname("Us");
            invalidRequest.setEmail("test@example.com");
            invalidRequest.setPassword("123456");

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }

        @Test
        @DisplayName("❌ Debe retornar 400 cuando faltan campos requeridos")
        void register_ShouldReturnBadRequest_WhenMissingFields() throws Exception {
            RegisterRequest emptyRequest = new RegisterRequest();

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyRequest)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).register(any());
        }
    }

    // ==================== TESTS: POST /auth/login ====================

    @Nested
    @DisplayName("POST /auth/login Tests")
    class LoginTests {

        @Test
        @DisplayName("✅ Debe iniciar sesión exitosamente")
        void login_ShouldReturnLoginResponse_WhenValid() throws Exception {
            when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiJ9.test-token"))
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Test"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.role").value("USER"));

            verify(authService, times(1)).login(any(LoginRequest.class));
        }

        @Test
        @DisplayName("❌ Debe retornar 400 cuando email es inválido")
        void login_ShouldReturnBadRequest_WhenInvalidEmail() throws Exception {
            LoginRequest invalidRequest = new LoginRequest();
            invalidRequest.setEmail("invalid-email");
            invalidRequest.setPassword("123456");

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any());
        }

        @Test
        @DisplayName("❌ Debe retornar 400 cuando faltan credenciales")
        void login_ShouldReturnBadRequest_WhenMissingCredentials() throws Exception {
            LoginRequest emptyRequest = new LoginRequest();

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyRequest)))
                    .andExpect(status().isBadRequest());

            verify(authService, never()).login(any());
        }


    }
}