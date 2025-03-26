package io.hhplus.tdd.Exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    String getCode();
    HttpStatus getStatus();
    String getMessage();
}
