package uk.gov.cslearning.catalogue.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class ErrorDto {
    private final Instant timestamp = Instant.now();
    private List<String> errors;
    private int status;
    private String message;

    public ErrorDto(List<String> errors, int status, String message) {
        this.errors = errors;
        this.status = status;
        this.message = message;
    }

    public ErrorDto(String error, int status, String message) {
        List<String> errors = new ArrayList<>();
        errors.add(error);
        this.errors = errors;
        this.status = status;
        this.message = message;
    }

    @JsonIgnore
    public ResponseEntity<Object> getAsResponseEntity() {
        return new ResponseEntity<>(this, HttpStatus.valueOf(getStatus()));
    }
}