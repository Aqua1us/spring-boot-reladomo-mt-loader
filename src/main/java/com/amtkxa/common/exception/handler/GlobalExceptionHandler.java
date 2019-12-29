package com.amtkxa.common.exception.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.amtkxa.common.exception.BadRequestException;
import com.amtkxa.common.exception.ReladomoConfigurationException;
import com.amtkxa.common.exception.ReladomoMTLoaderException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class.getName());
    private static String MESSAGE_INTERNAL_SERVER_ERROR =
            "We're sorry but it appears that there has been an internal server error while processing your request.";

    @ExceptionHandler({
            MissingServletRequestPartException.class,
            HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<String> handleBadRequestException(Exception e) {
        log.info("{} was occurred. message={}", e.getClass().getSimpleName(), e.getMessage(), e.getCause());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequestException(BadRequestException e) {
        log.info("BadRequestException was occurred. message={}", e.getMessage(), e.getCause());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(ReladomoMTLoaderException.class)
    public ResponseEntity<String> handleReladomoMTLoaderException(ReladomoMTLoaderException e) {
        log.error("ReladomoMtLoaderException was occurred. message={}", e.getMessage(), e.getCause());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MESSAGE_INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ReladomoConfigurationException.class)
    public ResponseEntity<String> handleReladomoConfigurationException(ReladomoConfigurationException e) {
        log.error("ReladomoConfigurationException was occurred. message={}", e.getMessage(), e.getCause());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MESSAGE_INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllException(Exception e) {
        log.error("{} was occurred. message={}", e.getClass().getSimpleName(), e.getMessage(), e.getCause());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(MESSAGE_INTERNAL_SERVER_ERROR);
    }
}
