package br.com.empiricus.error;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void preservesStatusAndMessageForExpectedApplicationErrors() {
        ResponseStatusException exception =
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa nao encontrada");

        var response = handler.handleResponseStatus(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(new ApiErrorResponse(404, "Mesa nao encontrada"), response.getBody());
    }

    @Test
    void doesNotExposeDatabaseDetailsForIntegrityConflicts() {
        var response = handler.handleDataIntegrityViolation();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(
                new ApiErrorResponse(409, "Operacao viola restricoes de dados"),
                response.getBody());
    }

    @Test
    void returnsGenericMessageForUnexpectedFailures() {
        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/mesas");

        var response = handler.handleUnexpected(new IllegalStateException("internal detail"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(new ApiErrorResponse(500, "Erro interno do servidor"), response.getBody());
    }
}
