package com.awbd.cakeshop.unitTests;

import com.awbd.cakeshop.DTOs.ReviewDTO;
import com.awbd.cakeshop.controllers.ReviewController;
import com.awbd.cakeshop.mappers.ReviewMapper;
import com.awbd.cakeshop.models.Cake;
import com.awbd.cakeshop.models.Review;
import com.awbd.cakeshop.models.User;
import com.awbd.cakeshop.services.ReviewService;
import com.awbd.cakeshop.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock private ReviewService reviewService;
    @Mock private ReviewMapper reviewMapper;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private ReviewController controller;

    @Test
    void list_returnsDtos() {
        Long cakeId = 1L;

        Review r = new Review();
        r.setId(11L);
        r.setRating(5);
        r.setComment("Super");
        r.setCreatedAt(Instant.now());
        Cake c = new Cake(); c.setId(cakeId);
        r.setCake(c);
        User u = new User(); u.setUsername("ana");
        r.setUser(u);

        ReviewDTO dto = new ReviewDTO(11L, 5, "Super", "ana", r.getCreatedAt());

        when(reviewService.getByCake(cakeId)).thenReturn(List.of(r));
        when(reviewMapper.toDto(r)).thenReturn(dto);

        List<ReviewDTO> out = controller.list(cakeId);

        assertEquals(1, out.size());
        assertEquals(11L, out.get(0).id());
        assertEquals("ana", out.get(0).username());
        verify(reviewService).getByCake(cakeId);
        verify(reviewMapper).toDto(r);
    }

    @Test
    void create_usesTokenUsername_andReturnsCreatedDto() {
        Long cakeId = 2L;
        String authHeader = "Bearer abc.def.ghi";
        when(jwtUtil.getUsernameFromToken("abc.def.ghi")).thenReturn("ileana");

        // review salvat de service
        Review saved = new Review();
        saved.setId(77L);
        saved.setRating(4);
        saved.setComment("Nice");
        saved.setCreatedAt(Instant.now());

        when(reviewService.add(eq(cakeId), eq("ileana"), eq(4), eq("Nice")))
                .thenReturn(saved);

        ReviewDTO dto = new ReviewDTO(77L, 4, "Nice", "ileana", saved.getCreatedAt());
        when(reviewMapper.toDto(saved)).thenReturn(dto);

        // construim request-ul pentru metoda controllerului
        ResponseEntity<ReviewDTO> resp = controller.create(
                cakeId,
                authHeader,
                // record intern ReviewCreateReq → simulăm cu clasa implicită a controllerului?
                // în test apelăm direct metoda, deci trimitem un obiect anonim compatibil:
                new Object() {
                    public int rating() { return 4; }
                    public String comment() { return "Nice"; }
                }
        );

        assertEquals(201, resp.getStatusCode().value());
        assertNotNull(resp.getHeaders().getLocation());
        assertTrue(resp.getHeaders().getLocation().toString()
                .startsWith("/api/cakes/2/reviews/"));
        assertEquals(dto, resp.getBody());
    }
}
