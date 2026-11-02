package dev.jamal.kasper_backend.Security;

import dev.jamal.kasper_backend.Service.ApiTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {

    private final ApiTokenService apiTokenService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String rawToken = extractToken(request);

        if (!StringUtils.hasText(rawToken)) {
            // Se rota /api/v1/ e não enviou token, rejeita com 401
            if (isProtectedMlPath(request)) {
                sendUnauthorizedResponse(response, "Token de Machine Learning não fornecido. Envie via header Authorization: Bearer <token>, X-API-KEY ou query param ?token=");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        try {
            ApiTokenPrincipal principal = apiTokenService.authenticate(rawToken);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_API"))
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
            if (isProtectedMlPath(request)) {
                sendUnauthorizedResponse(response, "Token de Machine Learning inválido ou expirado: " + exception.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // 1. Authorization: Bearer <token>
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader)) {
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7).trim();
            }
            return authHeader.trim();
        }

        // 2. Headers alternativos comuns
        String apiKeyHeader = request.getHeader("X-API-KEY");
        if (StringUtils.hasText(apiKeyHeader)) {
            return apiKeyHeader.trim();
        }

        String xTokenHeader = request.getHeader("X-Token");
        if (StringUtils.hasText(xTokenHeader)) {
            return xTokenHeader.trim();
        }

        String tokenHeader = request.getHeader("token");
        if (StringUtils.hasText(tokenHeader)) {
            return tokenHeader.trim();
        }

        // 3. Query param ?token=... ou ?api_key=...
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam.trim();
        }

        String apiKeyParam = request.getParameter("api_key");
        if (StringUtils.hasText(apiKeyParam)) {
            return apiKeyParam.trim();
        }

        return null;
    }

    private boolean isProtectedMlPath(HttpServletRequest request) {
        String path = request.getServletPath();
        if (!StringUtils.hasText(path)) {
            path = request.getRequestURI();
        }
        return path != null && path.contains("/api/v1/");
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String json = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message.replace("\"", "\\\""));
        response.getWriter().write(json);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !isProtectedMlPath(request);
    }
}
