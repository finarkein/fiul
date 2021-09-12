/**
 * Copyright (C) 2021 Finarkein Analytics Pvt. Ltd.
 * All rights reserved This software is the confidential and proprietary information of Finarkein Analytics Pvt. Ltd.
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Finarkein Analytics Pvt. Ltd.
 */
package io.finarkein.fiul.controller;

import io.finarkein.api.aa.exception.Error;
import io.finarkein.api.aa.exception.SystemException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.Timestamp;
import java.time.Instant;

@RestControllerAdvice
@Log4j2
public class ControllerAdvice {

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Error> handleBaseError(SystemException exception) {
        Error err = new Error();
        err.setTxnId(exception.txnId());
        err.setTimestamp(Timestamp.from(Instant.now()));
        err.setErrorCode(exception.errorCode().name());
        err.setErrorMessage(exception.getMessage());
        HttpStatus resolve = HttpStatus.resolve(exception.errorCode().httpStatusCode());
        if(resolve == null)
            resolve = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Error at server, error message:{}",exception.getMessage(), exception);
        return new ResponseEntity<>(err, resolve);
    }
}
