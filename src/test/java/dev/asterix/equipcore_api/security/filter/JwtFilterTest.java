package dev.asterix.equipcore_api.security.filter;

import dev.asterix.equipcore_api.config.JwtConfig;
import dev.asterix.equipcore_api.enumeration.UserRole;
import dev.asterix.equipcore_api.model.User;
import dev.asterix.equipcore_api.repository.UserRepository;
import dev.asterix.equipcore_api.security.JwtService;
import dev.asterix.equipcore_api.security.UserPrincipal;
import dev.asterix.equipcore_api.support.JwtTestSupport;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JwtFilterTest {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtConfig jwtConfig;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    private User user;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);

        user = User
                .builder()
                .firstName("Amine")
                .lastName("CHAKHAR")
                .email("amine.chakhar@gmail.com")
                .password("$2a$12$85NwZwUWcg9ve9Ry7MCmoefueK0fNTGyD2n5W7RdmAGqbGA9YoiU2")
                .role(UserRole.ADMIN)
                .isEnabled(true)
                .build();

        userRepository.save(user);

        userPrincipal = new UserPrincipal(user);
    }

    @AfterEach
    void tearDown() {

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotFilter_withPublicPath() throws ServletException {

        request.setServletPath("/auth/login");

        Assertions.assertTrue(jwtFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_withProtectedPath() throws ServletException {

        request.setServletPath("/equipments");

        boolean result = jwtFilter.shouldNotFilter(request);
        Assertions.assertFalse(result);
    }

    @Test
    void doFilterInternal_withValidToken() throws ServletException, IOException {

        String token = jwtService.generateToken(userPrincipal);
        request.addHeader("Authorization", "Bearer " + token);

        jwtFilter.doFilterInternal(request, response, filterChain);

        Assertions.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        Assertions.assertEquals(
                user.getEmail(),
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withAlreadyAuthenticatedContext() throws ServletException, IOException {

        String token = jwtService.generateToken(userPrincipal);
        request.addHeader("Authorization", "Bearer " + token);

        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        jwtFilter.doFilterInternal(request, response, filterChain);

        Assertions.assertEquals(
                existingAuth,
                SecurityContextHolder.getContext().getAuthentication()
        );
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withoutAuthorizationHeader() throws ServletException, IOException {

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_withNonBearerAuthorizationHeader() throws ServletException, IOException {

        request.addHeader("Authorization", "Basic basic-credentials");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);

        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_withExpiredToken() throws ServletException, IOException {

        String expiredToken = JwtTestSupport.generateExpiredToken(userPrincipal, jwtConfig);
        request.addHeader("Authorization", "Bearer " + expiredToken);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());

        Assertions.assertEquals(401, response.getStatus());
        Assertions.assertEquals("application/json", response.getContentType());
        Assertions.assertTrue(response.getContentAsString().contains("EXPIRED_JWT"));
        Assertions.assertTrue(response.getContentAsString().contains("JWT is expired"));
    }

    @Test
    void doFilterInternal_withMalformedToken() throws Exception {

        request.addHeader("Authorization", "Bearer not-a-real-token");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);

        Assertions.assertEquals(401, response.getStatus());
        Assertions.assertEquals("application/json", response.getContentType());
        Assertions.assertTrue(response.getContentAsString().contains("INVALID_JWT"));
        Assertions.assertTrue(response.getContentAsString().contains("JWT is invalid"));
    }

    @Test
    void doFilterInternal_withTokenForUnknownUser() throws Exception {

        User ghostUser = User
                .builder()
                .firstName("Ghost")
                .lastName("user")
                .email("ghost@gmail")
                .password("$2a$12$85NwZwUWcg9ve9Ry7MCmoefueK0fNTGyD2n5W7RdmAGqbGA9YoiU2")
                .role(UserRole.EMPLOYEE)
                .isEnabled(true)
                .build();

        String token = jwtService.generateToken(new UserPrincipal(ghostUser));

        request.addHeader("Authorization", "Bearer " + token);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);

        Assertions.assertEquals(401, response.getStatus());
        Assertions.assertTrue(response.getContentAsString().contains("INVALID_JWT"));
        Assertions.assertTrue(response.getContentAsString().contains("JWT is invalid"));
    }
}