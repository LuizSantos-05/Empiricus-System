package br.com.empiricus.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiSecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ApiErrorResponseWriter errorResponseWriter;

    public ApiSecurityExceptionHandler(ApiErrorResponseWriter errorResponseWriter) {
        this.errorResponseWriter = errorResponseWriter;
    }

    // Trata requisicoes sem autenticacao valida usando o formato de erro padronizado da API.
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        errorResponseWriter.write(response, HttpStatus.UNAUTHORIZED, "Autenticacao necessaria");
    }

    // Trata usuarios autenticados que nao possuem permissao para acessar o recurso.
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception) throws IOException {
        errorResponseWriter.write(response, HttpStatus.FORBIDDEN, "Acesso negado");
    }
}
