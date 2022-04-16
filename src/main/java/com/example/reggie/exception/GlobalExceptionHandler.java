package com.example.reggie.exception;

import com.example.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;

@Slf4j
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> processException(SQLIntegrityConstraintViolationException e) {
        if (e.getMessage().contains("Duplicate entry")) {
            log.error(e.getMessage());
            String[] s = e.getMessage().split(" ");
            log.error(Arrays.toString(s));
            return R.error(s[2] + "已存在");
        }
        return R.error("ERROR");
    }

    @ExceptionHandler(CustomException.class)
    public R<String> processException(CustomException e) {
        return R.error(e.getMessage());
    }
}
