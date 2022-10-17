package com.hzk.gulimall.product.exception;

import com.hzk.common.exception.BizCodeEnum;
import com.hzk.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kee
 * @version 1.0
 * @date 2022/8/29 16:22
 */
//@RestControllerAdvice = @ControllerAdvice + @ResponseBody
@Slf4j
@RestControllerAdvice(basePackages = "com.hzk.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handlerValidationException(MethodArgumentNotValidException e) {
        log.error("异常信息：{}，异常类型:{}", e.getMessage(), e.getClass());
        Map<String, String> map = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach((fieldError) -> {
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data", map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handlerException(Throwable e) {
        log.error("异常: {}", e);
        return R.error(BizCodeEnum.UNKONW_EXCEPTION.getCode(), BizCodeEnum.UNKONW_EXCEPTION.getMsg());
    }
}
