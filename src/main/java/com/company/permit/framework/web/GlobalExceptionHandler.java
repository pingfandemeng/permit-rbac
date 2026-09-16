package com.company.permit.framework.web;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<R<Void>> handleNotLogin(NotLoginException e) {
        log.warn("未认证: {}", e.getMessage());
        ErrorCode code = ErrorCode.A01001;
        if (e.getType() != null && e.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            code = ErrorCode.A01002;
        }
        return ResponseEntity.status(code.getHttpStatus()).body(R.fail(code));
    }

    @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
    public ResponseEntity<R<Void>> handleNotPermission(Exception e) {
        log.warn("无权限: {}", e.getMessage());
        return ResponseEntity.status(403).body(R.fail(ErrorCode.A01003));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<R<Void>> handleService(ServiceException e) {
        log.warn("业务异常 {}: {}", e.getErrorCode().getCode(), e.getMessage());
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(R.fail(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<R<Void>> handleValid(Exception e) {
        String msg = ErrorCode.C01003.getMsg();
        if (e instanceof MethodArgumentNotValidException) {
            msg = ((MethodArgumentNotValidException) e).getBindingResult().getFieldErrors()
                    .stream().map(FieldError::getDefaultMessage).collect(Collectors.joining("; "));
        } else if (e instanceof BindException) {
            msg = ((BindException) e).getBindingResult().getFieldErrors()
                    .stream().map(FieldError::getDefaultMessage).collect(Collectors.joining("; "));
        }
        return ResponseEntity.ok(R.fail(ErrorCode.C01003, msg));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<R<Void>> handleOptimistic(OptimisticLockingFailureException e) {
        return ResponseEntity.ok(R.fail(ErrorCode.C01002));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleOther(Exception e, HttpServletRequest request) {
        log.error("系统异常 {} {}", request.getMethod(), request.getRequestURI(), e);
        return ResponseEntity.status(500).body(R.fail(ErrorCode.C01001));
    }
}
