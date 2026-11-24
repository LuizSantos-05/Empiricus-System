package br.com.empiricus.auth;

import io.jsonwebtoken.JwtException;
import br.com.empiricus.error.ApiErrorResponseWriter;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @Test
    void rejectsMalformedTokensWithoutStoppingTheRequestChain() {
        SessionService sessionService = mock(SessionService.class);
        when(sessionService.authenticate("invalid"))
                .thenThrow(new JwtException("Malformed token"));
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                mock(JwtService.class), sessionService, mock(ApiErrorResponseWriter.class));
        MockHttpServletRequest request = authorizedRequest();

        assertDoesNotThrow(() -> filter.doFilter(
                request,
                new MockHttpServletResponse(),
                (servletRequest, servletResponse) -> {
                }));
    }

    @Test
    void returnsGenericServerErrorForDatabaseFailuresDuringTokenValidation() throws Exception {
        SessionService sessionService = mock(SessionService.class);
        when(sessionService.authenticate("invalid"))
                .thenThrow(new DataAccessResourceFailureException("Database unavailable"));
        ApiErrorResponseWriter errorResponseWriter = mock(ApiErrorResponseWriter.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                mock(JwtService.class), sessionService, errorResponseWriter);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertDoesNotThrow(() -> filter.doFilter(
                authorizedRequest(),
                response,
                (servletRequest, servletResponse) -> {
                }));
        verify(errorResponseWriter)
                .write(response, HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }

    private MockHttpServletRequest authorizedRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid");
        return request;
    }
}
