package com.genspark.account.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Employee not found!")
public class EmployeeNotFoundException extends RuntimeException {
}
