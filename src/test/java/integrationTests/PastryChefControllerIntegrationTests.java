package integrationTests;

import com.awbd.cakeshop.DTOs.PastryChefDTO;
import com.awbd.cakeshop.controllers.PastryChefController;
import com.awbd.cakeshop.exceptions.pastrychef.PastryChefNotFoundException;
import com.awbd.cakeshop.mappers.PastryChefMapper;
import com.awbd.cakeshop.models.PastryChef;
import com.awbd.cakeshop.services.PastryChefService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PastryChefControllerIntegrationTests {

    @Mock
    private PastryChefService pastryChefService;

    @Mock
    private PastryChefMapper pastryChefMapper;

    @InjectMocks
    private PastryChefController pastryChefController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(pastryChefController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreatePastryChef_Success() throws Exception {
        PastryChefDTO dto = new PastryChefDTO("Anna Sweet", "Expert in chocolate", LocalDate.of(1985, 4, 12));
        PastryChef entity = new PastryChef("Anna Sweet", "Expert in chocolate", LocalDate.of(1985, 4, 12));
        PastryChef created = new PastryChef("Anna Sweet", "Expert in chocolate", LocalDate.of(1985, 4, 12));
        created.setId(1L);

        when(pastryChefMapper.toEntity(any(PastryChefDTO.class))).thenReturn(entity);
        when(pastryChefService.create(any(PastryChef.class))).thenReturn(created);

        mockMvc.perform(post("/api/pastrychefs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/pastrychefs/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Anna Sweet"));
    }

    @Test
    void testGetPastryChefById_NotFound() throws Exception {
        when(pastryChefService.getById(999L)).thenThrow(new PastryChefNotFoundException("PastryChef with ID 999 not found"));

        mockMvc.perform(get("/api/pastrychefs/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("PastryChef with ID 999 not found"));
    }

    @Test
    void testGetAllPastryChefs_Empty() throws Exception {
        when(pastryChefService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/pastrychefs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdatePastryChef_Success() throws Exception {
        PastryChefDTO dto = new PastryChefDTO(1L, "Anna Sweet Updated", "Now with new recipes", LocalDate.of(1985, 4, 12));
        PastryChef entity = new PastryChef("Anna Sweet Updated", "Now with new recipes", LocalDate.of(1985, 4, 12));
        PastryChef updated = new PastryChef("Anna Sweet Updated", "Now with new recipes", LocalDate.of(1985, 4, 12));
        updated.setId(1L);

        when(pastryChefMapper.toEntity(any(PastryChefDTO.class))).thenReturn(entity);
        when(pastryChefService.update(eq(1L), any(PastryChef.class))).thenReturn(updated);

        mockMvc.perform(put("/api/pastrychefs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Anna Sweet Updated"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeletePastryChef_Success() throws Exception {
        doNothing().when(pastryChefService).delete(1L);

        mockMvc.perform(delete("/api/pastrychefs/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
