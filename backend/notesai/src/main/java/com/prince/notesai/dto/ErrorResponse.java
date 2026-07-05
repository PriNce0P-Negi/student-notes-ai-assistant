package com.prince.notesai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized error response returned by the GlobalExceptionHandler.
 */
@Data
@NoArgsConstructor
public class ErrorResponse {

    /** Short machine-readable error code (e.g. "FILE_TOO_LARGE"). */
    private String error;

    /** Human-readable error message. */
    private String message;

    /** UTC timestamp of when the error occurred. */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
