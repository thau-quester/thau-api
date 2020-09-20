package com.mgrin.thau;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.springframework.http.HttpStatus;

public class APIError extends RuntimeException {

    private static final long serialVersionUID = 8030233666972570016L;

    private String message;
    private String debugMessage;
    private HttpStatus status;
    private LocalDateTime timestamp;

    private APIError() {
        timestamp = LocalDateTime.now();
    }

    public APIError(Throwable ex) {
        this();
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.message = ex.getMessage();
        this.debugMessage = ex.getLocalizedMessage();

        if (!(ex instanceof APIError)) {
            ex.printStackTrace();
        }
    }

    public APIError(HttpStatus status, Throwable ex) {
        this();
        this.status = status;
        this.message = ex.getMessage();
        this.debugMessage = ex.getLocalizedMessage();
        if (!(ex instanceof APIError)) {
            ex.printStackTrace();
        }
    }

    public APIError(HttpStatus status, String message) {
        this();
        this.status = status;
        this.message = message;
        this.debugMessage = message;
    }

    public APIError(HttpStatus status, String message, Throwable ex) {
        this();
        this.status = status;
        this.message = message;
        this.debugMessage = ex.getLocalizedMessage();
        if (!(ex instanceof APIError)) {
            ex.printStackTrace();
        }
    }

    public HttpStatus getStatus() {
        return status;
    }

    public APIErrorDTO getDTO() {
        return new APIErrorDTO(this);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDebugMessage() {
        return debugMessage;
    }

    public void setDebugMessage(String debugMessage) {
        this.debugMessage = debugMessage;
    }

    public class APIErrorDTO {
        private String message;
        private String debugMessage;
        private HttpStatus status;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
        private LocalDateTime timestamp;

        public APIErrorDTO(APIError error) {
            this.message = error.message;
            this.debugMessage = error.debugMessage;
            this.status = error.status;
            this.timestamp = error.timestamp;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public HttpStatus getStatus() {
            return status;
        }

        public void setStatus(HttpStatus status) {
            this.status = status;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getDebugMessage() {
            return debugMessage;
        }

        public void setDebugMessage(String debugMessage) {
            this.debugMessage = debugMessage;
        }
    }

}