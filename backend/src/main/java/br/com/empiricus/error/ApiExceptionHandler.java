package br.com.empiricus.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // Preserva o status e a mensagem segura definidos para erros esperados da aplicacao.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String message = exception.getReason() == null ? status.getReasonPhrase() : exception.getReason();
        return response(status, message);
    }

    // Trata campos ausentes, parametros invalidos e falhas das validacoes declaradas nos endpoints.
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest(Exception exception) {
        String message = "Dados da requisicao invalidos";
        if (exception instanceof MethodArgumentNotValidException validationException) {
            message = validationException.getBindingResult().getAllErrors().stream()
                    .findFirst()
                    .map(ObjectError::getDefaultMessage)
                    .filter(errorMessage -> errorMessage != null && !errorMessage.isBlank())
                    .orElse(message);
        }
        return response(HttpStatus.BAD_REQUEST, message);
    }

    // Trata JSON malformado ou valores do corpo que nao podem ser convertidos para o DTO esperado.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableRequest() {
        return response(HttpStatus.BAD_REQUEST, "Corpo da requisicao invalido");
    }

    // Trata conflitos com restricoes de integridade sem expor detalhes do banco de dados.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation() {
        return response(HttpStatus.CONFLICT, "Operacao viola restricoes de dados");
    }

    // Trata requisicoes para recursos inexistentes com uma resposta 404 uniforme.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingResource() {
        return response(HttpStatus.NOT_FOUND, "Recurso nao encontrado");
    }

    // Trata metodos HTTP nao suportados pelo endpoint solicitado.
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedMethod() {
        return response(HttpStatus.METHOD_NOT_ALLOWED, "Metodo HTTP nao permitido");
    }

    // Trata tipos de conteudo que o endpoint nao aceita.
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedMediaType() {
        return response(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de conteudo nao suportado");
    }

    // Trata falhas inesperadas sem retornar detalhes internos e registra a excecao para diagnostico.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request) {
        logger.error(
                "Falha inesperada ao processar {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status.value(), message));
    }
}
