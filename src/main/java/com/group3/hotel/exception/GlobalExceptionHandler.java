package com.group3.hotel.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(BadRequestException.class)
    public String handleBadRequest(BadRequestException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/error";
    }

    @ExceptionHandler(Exception.class)
    public String handleServerError(Exception ex, Model model) {
        model.addAttribute("message", "Hệ thống đang có lỗi. Vui lòng kiểm tra lại dữ liệu hoặc log console.");
        return "error/500";
    }
}
