package com.web.book.version.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(HttpServletRequest request, Exception ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", "Error Interno del Servidor");
        mav.addObject("message", getMeaningfulMessage(ex));
        mav.addObject("url", request.getRequestURL());
        mav.setViewName("error");
        return mav;
    }

    @ExceptionHandler(RestClientException.class)
    public ModelAndView handleRestClientException(HttpServletRequest request, RestClientException ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", "Error de Conexión");
        mav.addObject("message", "No se pudo conectar con el servicio externo. Por favor, inténtalo más tarde.");
        mav.addObject("url", request.getRequestURL());
        mav.setViewName("error");
        return mav;
    }

    private String getMeaningfulMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isEmpty()) {
            return "Ha ocurrido un error inesperado. Por favor, intenta nuevamente.";
        }
        // Filtrar mensajes técnicos y mostrar mensajes más amigables
        if (message.contains("Connection refused")) {
            return "No se pudo establecer conexión con el servidor. Por favor, verifica tu conexión a internet.";
        }
        if (message.contains("404")) {
            return "El recurso solicitado no fue encontrado.";
        }
        if (message.contains("500")) {
            return "Error en el servidor. Por favor, intenta más tarde.";
        }
        return message;
    }
}