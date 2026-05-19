package sn.edu.ept.mediconnect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String path = request.getRequestURI();
        final String method = request.getMethod();

        logger.info("FILTER: Processing request - Path: " + path + ", Method: " + method);

        // Skip JWT check for public endpoints
        if (isPublicEndpoint(path, method)) {
            logger.info("JWT FILTER: Public endpoint detected, skipping authentication");
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader("Authorization");
        logger.info("JWT FILTER: Authorization header: " + (authHeader != null ? "Present" : "Missing"));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.error("JWT FILTER: No valid Authorization header found for protected endpoint: " + path);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Jeton d'authentification manquant ou invalide");
            return;
        }

//        try {
//            final String jwt = authHeader.substring(7);
//            logger.info("JWT FILTER: JWT Token extracted (length: " + jwt.length() + ")");
//
////            final String username = jwtService.extractUsername(jwt);
////            logger.info("JWT FILTER: Extracted username: " + username);
//
//            if (username == null) {
//                logger.error("=== JWT FILTER: Failed to extract username from JWT token");
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Jeton JWT invalide");
//                return;
//            }
//
//            // Vérifier si l'utilisateur est déjà authentifié
//            if (SecurityContextHolder.getContext().getAuthentication() != null) {
//                logger.info("=== JWT FILTER: User already authenticated, skipping");
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//            // Charger les détails de l'utilisateur
//            logger.info("=== JWT FILTER: Loading user details for username: " + username);
//            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
//            logger.info("=== JWT FILTER: User details loaded successfully. Authorities: " + userDetails.getAuthorities());
//
//            // Valider le token
//            logger.info("=== JWT FILTER: Validating JWT token...");
//            try {
//                boolean isTokenValid = jwtService.isTokenValid(jwt, userDetails);
//                logger.info("=== JWT FILTER: Token validation result: " + isTokenValid);
//
//                if (!isTokenValid) {
//                    logger.error("=== JWT FILTER: Token validation failed - Token is not valid");
//                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Jeton JWT expiré ou invalide");
//                    return;
//                }
//            } catch (Exception e) {
//                logger.error("=== JWT FILTER: Error during token validation: " + e.getMessage(), e);
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Erreur lors de la validation du jeton: " + e.getMessage());
//                return;
//            }
//
//            // Créer l'objet d'authentification
//            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                    userDetails,
//                    null,
//                    userDetails.getAuthorities()
//            );
//
//            // Définir les détails de l'authentification
//            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//            // Set the authentication in the security context
//            SecurityContext context = SecurityContextHolder.createEmptyContext();
//            context.setAuthentication(authToken);
//            SecurityContextHolder.setContext(context);
//
//            logger.info("=== JWT FILTER: User authenticated successfully: " + username + ". Roles: " + authToken.getAuthorities());
//            logger.info("=== JWT FILTER: SecurityContext set with authentication");
//
//            // Continue the filter chain with the authenticated user
//            filterChain.doFilter(request, response);
//
//        } catch (Exception e) {
//            logger.error("=== JWT FILTER: Authentication failed: ", e);
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Échec de l'authentification: " + e.getMessage());
//        }
    }

    /**
     * Détermine si un endpoint est public (ne nécessite pas d'authentification)
     */
    private boolean isPublicEndpoint(String path, String method) {
        // Auth endpoints
        if (path.startsWith("/api/auth/")) {
            return true;
        }

        // Swagger/API docs
        if (path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources/") ||
                path.equals("/swagger-ui.html")) {
            return true;
        }
        return false;
    }
}