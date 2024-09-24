package com.renansouza.folio.transactions.exceptions;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class TransactionsAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var req = ((ServletWebRequest) request).getRequest();
        var errorAttributes = getErrorAttributes(
                HttpStatus.BAD_REQUEST,
                Arrays.toString(ex.getDetailMessageArguments()),
                Optional.ofNullable(req.getPathInfo()).orElse(req.getServletPath()));

        return handleExceptionInternal(ex, errorAttributes, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = TransactionNotFoundException.class)
    protected ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
        var req = ((ServletWebRequest) request).getRequest();
        var errorAttributes = getErrorAttributes(
                HttpStatus.NOT_FOUND,
                ex.getLocalizedMessage(),
                Optional.ofNullable(req.getPathInfo()).orElse(req.getServletPath()));

        return handleExceptionInternal(ex, errorAttributes, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    private HashMap<String, Object> getErrorAttributes(HttpStatus status, String message, String path) {
        var errorAttributes = new HashMap<String, Object>();
        errorAttributes.put("timestamp", LocalDateTime.now());
        errorAttributes.put("status", status.value());
        errorAttributes.put("error", status);
        errorAttributes.put("message", message);
        errorAttributes.put("path", path);

        return errorAttributes;
    }
}