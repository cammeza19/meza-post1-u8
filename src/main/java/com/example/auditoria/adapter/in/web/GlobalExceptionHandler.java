package com.example.auditoria.adapter.in.web;

import com.example.auditoria.adapter.in.web.dto.ErrorResponse;
import com.example.auditoria.domain.valueobject.TransicionInvalidaException;
import com.example.auditoria.usecase.HallazgoNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Traduce las excepciones de dominio y de casos de uso a respuestas HTTP coherentes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({TransicionInvalidaException.class, IllegalStateException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> reglaDeNegocio(RuntimeException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return construir(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la peticion no es un JSON valido o contiene valores no permitidos", request);
    }

    @ExceptionHandler(HallazgoNotFoundException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(HallazgoNotFoundException ex, HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    private static ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje,
                                                           HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), mensaje, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
