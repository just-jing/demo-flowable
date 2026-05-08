package com.example.demoflowable.configs;

import com.example.demoflowable.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BizException.class)
    public ResponseEntity<Map<String, Object>> bizExceptionHandler(BizException ex) {
        log.warn("[bizExceptionHandler] code={} message={}", ex.getCode(), ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("code", ex.getCode());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validationExceptionHandler(MethodArgumentNotValidException ex) {
        log.warn("[validationExceptionHandler]", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("code", 1005);
        body.put("message", ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Map<String, Object>> exceptionHandler(Exception ex) {
        log.error("[exceptionHandler]", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("code", 500);
        body.put("message", "系统繁忙，请稍后再试");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
