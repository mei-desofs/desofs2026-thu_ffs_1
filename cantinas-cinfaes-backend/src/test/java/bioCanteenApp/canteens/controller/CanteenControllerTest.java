package bioCanteenApp.canteens.controller;

import bioCanteenApp.canteens.dto.CanteenDTO;
import bioCanteenApp.canteens.service.ICanteenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CanteenControllerTest {

    private ICanteenService service;
    private CanteenController controller;

    @BeforeEach
    void setUp() {
        service = mock(ICanteenService.class);
        controller = new CanteenController(service);
    }

    @Test
    void shouldCreateCanteen() {
        CanteenDTO request = new CanteenDTO();
        CanteenDTO created = new CanteenDTO();

        when(service.createCanteen(request)).thenReturn(created);

        ResponseEntity<CanteenDTO> response = controller.createCanteen(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(created, response.getBody());

        verify(service).createCanteen(request);
    }

    @Test
    void shouldGetAllCanteens() {
        List<CanteenDTO> canteens = List.of(
                new CanteenDTO(),
                new CanteenDTO()
        );

        when(service.getAllCanteens()).thenReturn(canteens);

        ResponseEntity<List<CanteenDTO>> response = controller.getAllCanteens();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(canteens, response.getBody());

        verify(service).getAllCanteens();
    }

    @Test
    void shouldGetCanteenById() {
        CanteenDTO dto = new CanteenDTO();

        when(service.getById(1L)).thenReturn(dto);

        ResponseEntity<CanteenDTO> response = controller.getCanteenById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(service).getById(1L);
    }

    @Test
    void shouldQuarantineVillage() {
        List<CanteenDTO> canteens = List.of(new CanteenDTO());

        when(service.quarantineCanteensByVillage("ANSIAES"))
                .thenReturn(canteens);

        ResponseEntity<List<CanteenDTO>> response =
                controller.quarantineVillage("ANSIAES");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(canteens, response.getBody());

        verify(service).quarantineCanteensByVillage("ANSIAES");
    }

    @Test
    void shouldUnquarantineVillage() {
        List<CanteenDTO> canteens = List.of(new CanteenDTO());

        when(service.unquarantineCanteensByVillage("ANSIAES"))
                .thenReturn(canteens);

        ResponseEntity<List<CanteenDTO>> response =
                controller.unquarantineVillage("ANSIAES");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(canteens, response.getBody());

        verify(service).unquarantineCanteensByVillage("ANSIAES");
    }

    @Test
    void shouldGetCanteensByMunicipality() {
        List<CanteenDTO> canteens = List.of(new CanteenDTO());

        when(service.getByMunicipality("RESENDE"))
                .thenReturn(canteens);

        ResponseEntity<List<CanteenDTO>> response =
                controller.getCanteensByMunicipality("RESENDE");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(canteens, response.getBody());

        verify(service).getByMunicipality("RESENDE");
    }

    @Test
    void shouldGetCanteensByVillage() {
        List<CanteenDTO> canteens = List.of(new CanteenDTO());

        when(service.getByVillage("ANSIAES"))
                .thenReturn(canteens);

        ResponseEntity<List<CanteenDTO>> response =
                controller.getCanteensByVillage("ANSIAES");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(canteens, response.getBody());

        verify(service).getByVillage("ANSIAES");
    }
}