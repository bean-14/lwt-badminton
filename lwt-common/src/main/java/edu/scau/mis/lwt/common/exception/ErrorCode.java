package edu.scau.mis.lwt.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "success"),
    ERROR(500, "error"),
    PARAM_ERROR(400, "Parameter error"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not found"),
    USER_NOT_EXIST(1001, "User does not exist"),
    USER_ALREADY_EXIST(1002, "User already exists"),
    PASSWORD_ERROR(1003, "Password error"),
    TOKEN_EXPIRED(1004, "Token expired"),
    TOKEN_INVALID(1005, "Token invalid"),
    PERMISSION_DENIED(1006, "Permission denied");

    private final Integer code;
    private final String message;
}
