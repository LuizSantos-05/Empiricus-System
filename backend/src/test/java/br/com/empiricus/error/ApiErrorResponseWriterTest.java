package br.com.empiricus.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiErrorResponseWriterTest {

    @Test
    void writesStatusAndStandardizedJsonBody() throws Exception {
        ApiErrorResponseWriter writer = new ApiErrorResponseWriter(new ObjectMapper());
        MockHttpServletResponse response = new MockHttpServletResponse();

        writer.write(response, HttpStatus.UNAUTHORIZED, "Autenticacao necessaria");

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        assertEquals(
                new ApiErrorResponse(401, "Autenticacao necessaria"),
                new ObjectMapper().readValue(response.getContentAsByteArray(), ApiErrorResponse.class));
    }
}
