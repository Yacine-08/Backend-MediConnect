package sn.edu.ept.mediconnect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sn.edu.ept.mediconnect.auth.services.JwtService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String path = request.getRequestURI();

        logger.info("JWT FILTER - Path: {}", path);

        // Public endpoints
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token manquant"
            );
            return;
        }

        try {
            final String jwt = authHeader.substring(7);

            final String username = jwtService.extractUsername(jwt);

            if (username == null) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Token invalide"
                );
                return;
            }

            // si déjà authentifié
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            if (!jwtService.isTokenValid(jwt, userDetails)) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Token expiré ou invalide"
                );
                return;
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authToken.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder.getContext()
                    .setAuthentication(authToken);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Erreur auth JWT", e);

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Erreur d'authentification"
            );
        }
    }

    private boolean isPublicEndpoint(String path) {

        return path.startsWith("/api/auth/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources/");
    }
}