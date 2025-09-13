package com.awbd.cakeshop.unitTests;

import com.awbd.cakeshop.DTOs.SaleDTO;
import com.awbd.cakeshop.controllers.SaleController;
import com.awbd.cakeshop.mappers.SaleMapper;
import com.awbd.cakeshop.models.Sale;
import com.awbd.cakeshop.services.SaleService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleControllerTest {

    @Mock
    private SaleService saleService;

    @Mock
    private SaleMapper saleMapper;

    @InjectMocks
    private SaleController saleController;

    private Sale testSale;
    private SaleDTO testSaleDTO;
    private List<Long> categoryIds;

    @BeforeEach
    void setup() {
        categoryIds = Arrays.asList(1L, 2L);

        testSale = new Sale();
        testSale.setId(1L);
        testSale.setSaleCode("SALE2024");
        testSale.setDiscountPercentage(20.0);
        testSale.setStartDate(LocalDate.of(2024, 1, 1));
        testSale.setEndDate(LocalDate.of(2024, 12, 31));
        testSale.setDescription("Winter Sale");
        testSale.setIsActive(true);

        testSaleDTO = new SaleDTO();
        testSaleDTO.setId(1L);
        testSaleDTO.setSaleCode("SALE2024");
        testSaleDTO.setDiscountPercentage(20.0);
        testSaleDTO.setStartDate(LocalDate.of(2024, 1, 1));
        testSaleDTO.setEndDate(LocalDate.of(2024, 12, 31));
        testSaleDTO.setDescription("Winter Sale");
        testSaleDTO.setIsActive(true);
        testSaleDTO.setCategoryIds(categoryIds);
    }

    @Test
    void shouldCreateSaleSuccessfully() {
        when(saleMapper.toEntity(testSaleDTO)).thenReturn(testSale);
        when(saleService.create(testSale, categoryIds)).thenReturn(testSale);
        when(saleService.getByIdWithStatusCheck(1L)).thenReturn(testSale);
        when(saleMapper.toDto(testSale)).thenReturn(testSaleDTO);

        ResponseEntity<SaleDTO> response = saleController.createSale(testSaleDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testSaleDTO, response.getBody());
        assertEquals(URI.create("/api/sales/1"), response.getHeaders().getLocation());

        verify(saleMapper).toEntity(testSaleDTO);
        verify(saleService).create(testSale, categoryIds);
        verify(saleService).getByIdWithStatusCheck(1L);
        verify(saleMapper).toDto(testSale);
    }

    @Test
    void shouldReturnSaleById() {
        when(saleService.getById(1L)).thenReturn(testSale);
        when(saleService.getByIdWithStatusCheck(1L)).thenReturn(testSale);
        when(saleMapper.toDto(testSale)).thenReturn(testSaleDTO);

        ResponseEntity<SaleDTO> response = saleController.getSaleById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testSaleDTO, response.getBody());

        verify(saleService).getById(1L);
        verify(saleService).getByIdWithStatusCheck(1L);
        verify(saleMapper).toDto(testSale);
    }

    @Test
    void shouldReturnAllSales() {
        when(saleService.getAll()).thenReturn(List.of(testSale));
        when(saleService.getAllWithStatusCheck(List.of(testSale))).thenReturn(List.of(testSale));
        when(saleMapper.toDtoList(List.of(testSale))).thenReturn(List.of(testSaleDTO));

        List<SaleDTO> result = saleController.getAllSales();

        assertEquals(1, result.size());
        assertEquals("SALE2024", result.get(0).getSaleCode());

        verify(saleService).getAll();
        verify(saleService).getAllWithStatusCheck(List.of(testSale));
        verify(saleMapper).toDtoList(List.of(testSale));
    }

    @Test
    void shouldReturnEmptyListWhenNoSales() {
        when(saleService.getAll()).thenReturn(List.of());
        when(saleService.getAllWithStatusCheck(List.of())).thenReturn(List.of());
        when(saleMapper.toDtoList(List.of())).thenReturn(List.of());

        List<SaleDTO> result = saleController.getAllSales();

        assertTrue(result.isEmpty());

        verify(saleService).getAll();
        verify(saleService).getAllWithStatusCheck(List.of());
        verify(saleMapper).toDtoList(List.of());
    }

    @Test
    void shouldReturnActiveSalesOnly() {
        when(saleService.getAllActiveSales()).thenReturn(List.of(testSale));
        when(saleService.getAllWithStatusCheck(List.of(testSale))).thenReturn(List.of(testSale));
        when(saleMapper.toDtoList(List.of(testSale))).thenReturn(List.of(testSaleDTO));

        List<SaleDTO> result = saleController.getActiveSales();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsActive());

        verify(saleService).getAllActiveSales();
        verify(saleService).getAllWithStatusCheck(List.of(testSale));
        verify(saleMapper).toDtoList(List.of(testSale));
    }

    @Test
    void shouldReturnEmptyListForNoActiveSales() {
        when(saleService.getAllActiveSales()).thenReturn(List.of());
        when(saleService.getAllWithStatusCheck(List.of())).thenReturn(List.of());
        when(saleMapper.toDtoList(List.of())).thenReturn(List.of());

        List<SaleDTO> result = saleController.getActiveSales();

        assertTrue(result.isEmpty());

        verify(saleService).getAllActiveSales();
        verify(saleService).getAllWithStatusCheck(List.of());
        verify(saleMapper).toDtoList(List.of());
    }
}
