package com.awbd.cakeshop.unitTests;

import com.awbd.cakeshop.DTOs.PastryChefDTO;
import com.awbd.cakeshop.controllers.PastryChefController;
import com.awbd.cakeshop.models.PastryChef;
import com.awbd.cakeshop.services.PastryChefService;
import com.awbd.cakeshop.mappers.PastryChefMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PastryChefControllerTest {

    @Mock
    private PastryChefService pastryChefService;

    @Mock
    private PastryChefMapper pastryChefMapper;

    @InjectMocks
    private PastryChefController pastryChefController;

    private PastryChef pastryChef;
    private PastryChefDTO pastryChefDTO;

    @BeforeEach
    void setUp() {
        pastryChef = new PastryChef();
        pastryChef.setId(1L);
        pastryChef.setName("Chef Dulcica");
        pastryChef.setBiography("Expert în prăjituri și torturi artizanale");
        pastryChef.setBirthDate(LocalDate.of(1985, 3, 21));

        pastryChefDTO = new PastryChefDTO();
        pastryChefDTO.setId(1L);
        pastryChefDTO.setName("Chef Dulcica");
        pastryChefDTO.setBiography("Expert în prăjituri și torturi artizanale");
        pastryChefDTO.setBirthDate(LocalDate.of(1985, 3, 21));
    }

    @Test
    void createPastryChef_Success() {
        PastryChef createdChef = new PastryChef();
        createdChef.setId(1L);
        createdChef.setName("Chef Dulcica");

        when(pastryChefMapper.toEntity(pastryChefDTO)).thenReturn(pastryChef);
        when(pastryChefService.create(pastryChef)).thenReturn(createdChef);

        ResponseEntity<PastryChef> result = pastryChefController.createPastryChef(pastryChefDTO);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(createdChef, result.getBody());
        assertEquals(URI.create("/api/pastry-chefs/1"), result.getHeaders().getLocation());
        verify(pastryChefMapper, times(1)).toEntity(pastryChefDTO);
        verify(pastryChefService, times(1)).create(pastryChef);
    }

    @Test
    void getPastryChefById_Success() {
        when(pastryChefService.getById(1L)).thenReturn(pastryChef);

        ResponseEntity<PastryChef> result = pastryChefController.getPastryChefById(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(pastryChef, result.getBody());
        verify(pastryChefService, times(1)).getById(1L);
    }

    @Test
    void getAllPastryChefs_Success() {
        List<PastryChef> chefs = Arrays.asList(pastryChef);

        when(pastryChefService.getAll()).thenReturn(chefs);

        List<PastryChef> result = pastryChefController.getAllPastryChefs();

        assertEquals(1, result.size());
        assertEquals("Chef Dulcica", result.get(0).getName());
        verify(pastryChefService, times(1)).getAll();
    }

    @Test
    void updatePastryChef_Success() {
        PastryChef updatedChef = new PastryChef();
        updatedChef.setId(1L);
        updatedChef.setName("Chef Vanilie");

        when(pastryChefMapper.toEntity(pastryChefDTO)).thenReturn(pastryChef);
        when(pastryChefService.update(1L, pastryChef)).thenReturn(updatedChef);

        ResponseEntity<PastryChef> result = pastryChefController.updatePastryChef(1L, pastryChefDTO);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(updatedChef, result.getBody());
        verify(pastryChefMapper, times(1)).toEntity(pastryChefDTO);
        verify(pastryChefService, times(1)).update(1L, pastryChef);
    }

    @Test
    void deletePastryChef_Success() {
        doNothing().when(pastryChefService).delete(1L);

        ResponseEntity<Void> result = pastryChefController.deletePastryChef(1L);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(pastryChefService, times(1)).delete(1L);
    }
}
