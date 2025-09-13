package com.awbd.cakeshop.unitTests;

import com.awbd.cakeshop.DTOs.CakeDTO;
import com.awbd.cakeshop.controllers.CakeController;
import com.awbd.cakeshop.mappers.CakeMapper;
import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.services.CakeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CakeControllerTest {

    @Mock
    private CakeService cakeService;

    @Mock
    private CakeMapper cakeMapper;

    @InjectMocks
    private CakeController cakeController;

    @Test
    void getAllCakes_Success() {
        Cake cake1 = new Cake(); cake1.setId(1L); cake1.setName("Cake 1");
        Cake cake2 = new Cake(); cake2.setId(2L); cake2.setName("Cake 2");
        List<Cake> cakes = Arrays.asList(cake1, cake2);

        CakeDTO dto1 = new CakeDTO(); dto1.setId(1L); dto1.setName("Cake 1");
        CakeDTO dto2 = new CakeDTO(); dto2.setId(2L); dto2.setName("Cake 2");
        List<CakeDTO> dtoList = Arrays.asList(dto1, dto2);

        when(cakeService.getAllCakes()).thenReturn(cakes);
        when(cakeMapper.toDtoList(cakes)).thenReturn(dtoList);

        // ⬇️ acum metoda cere 2 parametri
        List<CakeDTO> result = cakeController.getAllCakes(null, null);

        assertEquals(2, result.size());
        assertEquals("Cake 1", result.get(0).getName());
        assertEquals("Cake 2", result.get(1).getName());
        verify(cakeService).getAllCakes();
        verify(cakeMapper).toDtoList(cakes);
    }

    @Test
    void getAllCakes_FilterByCategory_Success() {
        Long categoryId = 5L;

        Cake cake = new Cake(); cake.setId(10L); cake.setName("Cat Cake");
        List<Cake> cakes = List.of(cake);

        CakeDTO dto = new CakeDTO(); dto.setId(10L); dto.setName("Cat Cake");
        List<CakeDTO> dtoList = List.of(dto);

        when(cakeService.getCakesByCategoryId(categoryId)).thenReturn(cakes);
        when(cakeMapper.toDtoList(cakes)).thenReturn(dtoList);

        List<CakeDTO> result = cakeController.getAllCakes(categoryId, null);

        assertEquals(1, result.size());
        assertEquals("Cat Cake", result.get(0).getName());
        verify(cakeService).getCakesByCategoryId(categoryId);
        verify(cakeMapper).toDtoList(cakes);
    }

    @Test
    void getAllCakes_SearchByQ_Success() {
        String q = "Chocolate";

        Cake cake = new Cake(); cake.setId(1L); cake.setName("Chocolate Cake");
        List<Cake> cakes = List.of(cake);

        CakeDTO dto = new CakeDTO(); dto.setId(1L); dto.setName("Chocolate Cake");
        List<CakeDTO> dtoList = List.of(dto);

        when(cakeService.searchByName(q)).thenReturn(cakes);
        when(cakeMapper.toDtoList(cakes)).thenReturn(dtoList);

        List<CakeDTO> result = cakeController.getAllCakes(null, q);

        assertEquals(1, result.size());
        assertEquals("Chocolate Cake", result.get(0).getName());
        verify(cakeService).searchByName(q);
        verify(cakeMapper).toDtoList(cakes);
    }

    @Test
    void getOne_Found() {
        Long id = 42L;
        Cake entity = new Cake(); entity.setId(id); entity.setName("Lemon Cake");
        CakeDTO dto = new CakeDTO(); dto.setId(id); dto.setName("Lemon Cake");

        when(cakeService.findById(id)).thenReturn(Optional.of(entity));
        when(cakeMapper.toDto(entity)).thenReturn(dto);

        ResponseEntity<CakeDTO> response = cakeController.getOne(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(cakeService).findById(id);
        verify(cakeMapper).toDto(entity);
    }

    @Test
    void getOne_NotFound() {
        Long id = 404L;
        when(cakeService.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<CakeDTO> response = cakeController.getOne(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(cakeService).findById(id);
        verifyNoInteractions(cakeMapper);
    }

    @Test
    void searchCakes_Success() {
        String name = "Chocolate";
        Cake cake = new Cake(); cake.setId(1L); cake.setName("Chocolate Cake");
        List<Cake> cakes = List.of(cake);

        CakeDTO dto = new CakeDTO(); dto.setId(1L); dto.setName("Chocolate Cake");
        List<CakeDTO> dtoList = List.of(dto);

        when(cakeService.searchByName(name)).thenReturn(cakes);
        when(cakeMapper.toDtoList(cakes)).thenReturn(dtoList);

        List<CakeDTO> result = cakeController.searchCakes(name);

        assertEquals(1, result.size());
        assertEquals("Chocolate Cake", result.get(0).getName());
        verify(cakeService).searchByName(name);
        verify(cakeMapper).toDtoList(cakes);
    }

    @Test
    void getAvailableCakes_Success() {
        Cake cake = new Cake(); cake.setId(1L); cake.setName("Fruit Cake");
        List<Cake> cakes = List.of(cake);

        CakeDTO dto = new CakeDTO(); dto.setId(1L); dto.setName("Fruit Cake");
        List<CakeDTO> dtoList = List.of(dto);

        when(cakeService.getCakesInStock()).thenReturn(cakes);
        when(cakeMapper.toDtoList(cakes)).thenReturn(dtoList);

        List<CakeDTO> result = cakeController.getAvailableCakes();

        assertEquals(1, result.size());
        assertEquals("Fruit Cake", result.get(0).getName());
        verify(cakeService).getCakesInStock();
        verify(cakeMapper).toDtoList(cakes);
    }

    @Test
    void deleteCake_Success() {
        Long cakeId = 1L;

        doNothing().when(cakeService).deleteCake(cakeId);

        ResponseEntity<Void> response = cakeController.deleteCake(cakeId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(cakeService).deleteCake(cakeId);
    }

    @Test
    void addCake_Success() {
        CakeDTO inputDto = new CakeDTO();
        inputDto.setName("Strawberry Cake");

        Cake entity = new Cake(); entity.setName("Strawberry Cake");

        Cake savedEntity = new Cake(); savedEntity.setId(100L); savedEntity.setName("Strawberry Cake");

        CakeDTO savedDto = new CakeDTO(); savedDto.setId(100L); savedDto.setName("Strawberry Cake");

        when(cakeMapper.toEntity(inputDto)).thenReturn(entity);
        when(cakeService.addCake(entity)).thenReturn(savedEntity);
        when(cakeMapper.toDto(savedEntity)).thenReturn(savedDto);

        ResponseEntity<CakeDTO> response = cakeController.addCake(inputDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedDto, response.getBody());
        assertEquals(URI.create("/api/cakes/100"), response.getHeaders().getLocation());

        verify(cakeMapper).toEntity(inputDto);
        verify(cakeService).addCake(entity);
        verify(cakeMapper).toDto(savedEntity);
    }

    @Test
    void updateCake_Success() {
        Long id = 7L;

        CakeDTO requestDto = new CakeDTO();
        requestDto.setName("Updated Name");

        Cake toUpdate = new Cake();
        Cake updated = new Cake(); updated.setId(id); updated.setName("Updated Name");

        CakeDTO responseDto = new CakeDTO(); responseDto.setId(id); responseDto.setName("Updated Name");

        when(cakeMapper.toEntity(requestDto)).thenReturn(toUpdate);
        when(cakeService.update(id, toUpdate)).thenReturn(updated);
        when(cakeMapper.toDto(updated)).thenReturn(responseDto);

        ResponseEntity<CakeDTO> response = cakeController.updateCake(id, requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        verify(cakeMapper).toEntity(requestDto);
        verify(cakeService).update(id, toUpdate);
        verify(cakeMapper).toDto(updated);
    }
}
