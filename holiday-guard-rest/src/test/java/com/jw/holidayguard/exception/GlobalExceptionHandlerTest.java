package com.jw.holidayguard.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private ObjectError objectError;

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        // given
        String errorMessage = "Name is required";
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(objectError));
        when(objectError.getDefaultMessage()).thenReturn(errorMessage);

        // when
        ProblemDetail problem = globalExceptionHandler.handleValidationError(methodArgumentNotValidException);

        // then
        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Validation Error", problem.getTitle());
        assertEquals(errorMessage, problem.getDetail());
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        // given
        String errorMessage = "Invalid schedule ID";

        // when
        ProblemDetail problem = globalExceptionHandler.handleIllegalArgument(new IllegalArgumentException(errorMessage));

        // then
        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Invalid Request", problem.getTitle());
        assertEquals(errorMessage, problem.getDetail());
    }

    @Test
    void shouldHandleIllegalStateException() {
        // given
        String errorMessage = "Schedule is in an invalid state";

        // when
        ProblemDetail problem = globalExceptionHandler.handleIllegalState(new IllegalStateException(errorMessage));

        // then
        assertEquals(HttpStatus.CONFLICT.value(), problem.getStatus());
        assertEquals("Invalid State", problem.getTitle());
        assertEquals(errorMessage, problem.getDetail());
    }
}
