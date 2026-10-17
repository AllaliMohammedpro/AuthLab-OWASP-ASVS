package com.irisi.authlab.exception;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(ApiException.class)
 public ResponseEntity<Map<String,String>> handle(ApiException error){return ResponseEntity.status(error.getStatus()).body(Map.of("message",error.getMessage()));}
 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<Map<String,String>> validation(){return ResponseEntity.badRequest().body(Map.of("message","Veuillez vérifier les champs du formulaire."));}
}
