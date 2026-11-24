package br.com.empiricus.auth;

import io.jsonwebtoken.JwtException;
import br.com.empiricus.error.ApiErrorResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final SessionService sessionService;
    private final ApiErrorResponseWriter errorResponseWriter;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            SessionService sessionService,
            ApiErrorResponseWriter errorResponseWriter) {
        this.jwtService = jwtService;
        this.sessionService = sessionService;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        // Rotas publicas seguem sem token; as demais dependem de uma autenticacao valida.
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                User user = sessionService.authenticate(token).orElse(null);
                if (user != null) {
                    // Registra o usuario no contexto para que o Spring autorize a requisicao.
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            // Trata falhas de persistencia ao verificar a sessao e evita responder como se o token fosse invalido.
            } catch (DataAccessException exception) {
                logger.error("Falha de acesso ao banco durante a validacao da sessao JWT", exception);
                errorResponseWriter.write(response, HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
                return;
            // Trata tokens JWT malformados ou com claims invalidas sem ocultar falhas de persistencia.
            } catch (JwtException | IllegalArgumentException exception) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}