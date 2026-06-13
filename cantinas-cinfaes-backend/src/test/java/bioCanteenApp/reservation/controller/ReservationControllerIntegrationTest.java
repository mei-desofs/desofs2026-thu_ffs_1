package bioCanteenApp.reservation.controller;

import bioCanteenApp.reservation.domain.ReservationStatus;
import bioCanteenApp.reservation.dto.ReservationDTO;
import bioCanteenApp.reservation.repository.ReservationRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate; // <-- IMPORTANTE: Import do JdbcTemplate
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@WithMockUser(roles = "ADMIN")
@Transactional
class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationRepo reservationRepo;


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Deve retornar uma lista de todas as reservas")
    void shouldGetAllReservations() throws Exception {
        mockMvc.perform(get("/api/reservations")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Deve retornar uma reserva específica pelo seu ID")
    void shouldGetReservationById() throws Exception {
        mockMvc.perform(get("/api/reservations/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    @DisplayName("Deve criar uma nova reserva com sucesso")
    void shouldCreateReservation() throws Exception {

        jdbcTemplate.update("DELETE FROM reservations");

        // 2. Preparar os dados
        ReservationDTO newReservation = new ReservationDTO();
        newReservation.setUserId(2L);
        newReservation.setMenuEntryDishId(2L);
        newReservation.setReservationDateTime(LocalDateTime.now().plusDays(1));
        newReservation.setStatus(ReservationStatus.CONFIRMED);

        // 3. Executar o POST
        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReservation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.menuEntryDishId").value(2))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("Deve falhar ao criar reserva duplicada (409 Conflict)")
    void shouldFailWhenReservationAlreadyExists() throws Exception {

        ReservationDTO duplicateReservation = new ReservationDTO();
        duplicateReservation.setUserId(1L);
        duplicateReservation.setMenuEntryDishId(1L);
        duplicateReservation.setReservationDateTime(LocalDateTime.now().plusDays(1));
        duplicateReservation.setStatus(ReservationStatus.CONFIRMED);

        mockMvc.perform(post("/api/reservations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateReservation)))
                .andExpect(status().isConflict()) // Espera o erro 409
                .andExpect(jsonPath("$.error").value("Reservation already exists"));
    }
}