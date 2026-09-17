package dev.asterix.equipcore_api.security.filter;

import dev.asterix.equipcore_api.enumeration.ErrorCode;
import dev.asterix.equipcore_api.dto.error.ErrorResponse;
import dev.asterix.equipcore_api.security.JwtService;
import dev.asterix.equipcore_api.security.UserPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        return HttpMethod.POST.matches(request.getMethod()) && "/auth".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {

            String email = jwtService.extractSubject(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserPrincipal userPrincipal = (UserPrincipal) userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(userPrincipal.getUsername(), userPrincipal.getUser().getTokenVersion(), token)) {

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException exception) {

            writeErrorResponse(response, ErrorCode.EXPIRED_JWT, "JWT is expired");
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException exception) {

            writeErrorResponse(response, ErrorCode.INVALID_JWT, "JWT is invalid");
        }

    }

    // Helper to build and send a JSON error response
    private void writeErrorResponse(
            HttpServletResponse response,
            ErrorCode errorCode,
            String message
    ) throws IOException {

        ErrorResponse errorResponse = new ErrorResponse(message, errorCode);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
