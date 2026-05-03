package edu.scau.mis.lwt.common.exception;

import edu.scau.mis.lwt.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public R<?> handleBusinessException(BusinessException e) {
        log.error("Business exception: {}", e.getMessage());
        return R.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<?> handleException(Exception e) {
        log.error("Unknown exception: {}", e.getMessage(), e);
        return R.error("System error");
    }
}
