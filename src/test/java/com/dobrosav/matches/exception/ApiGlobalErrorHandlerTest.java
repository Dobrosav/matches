package com.dobrosav.matches.exception;

import com.dobrosav.matches.model.pojo.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiGlobalErrorHandlerTest {

    @InjectMocks
    private ApiGlobalErrorHandler errorHandler;

    @Test
    void testHandleServiceException() {
        ServiceException ex = new ServiceException(ErrorType.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        ResponseEntity<ApiErrorResponse> response = errorHandler.handleServiceException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorType.USER_NOT_FOUND.getMessage(), response.getBody().getMessage());
    }

    @Test
    void testHandleNoHandlerFoundException() {
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/test", null);
        ApiErrorResponse response = errorHandler.handleNoHandlerFoundException(ex);

        assertNotNull(response);
        assertEquals(ErrorType.UNRESOLVED_ERROR.getMessage(), response.getMessage());
    }

    @Test
    void testHandleHttpRequestMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");
        ApiErrorResponse response = errorHandler.handleHttpRequestMethodNotSupportedException(ex);

        assertNotNull(response);
        // The implementation appends ":Request method 'POST' not supported" to the message
        // ErrorType.NOT_SUPPORTED_HTTP_METHOD.getMessage() + ":" + ex.getMessage()
        String expectedMessage = ErrorType.NOT_SUPPORTED_HTTP_METHOD.getMessage() + ":" + ex.getMessage();
        assertEquals(expectedMessage, response.getMessage());
    }

    @Test
    void testHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Bad request");
        ApiErrorResponse response = errorHandler.handleHttpMessageNotReadableException(ex);

        assertNotNull(response);
        String expectedMessage = ErrorType.NOT_VALID_REQUEST_FORMAT.getMessage() + ":" + ex.getMessage();
        assertEquals(expectedMessage, response.getMessage());
    }

    @Test
    void testHandleMethodArgumentNotValid() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "defaultMessage");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        
        ApiErrorResponse response = errorHandler.handleMethodArgumentNotValid(ex);
        
        assertNotNull(response);
        // Expecting ErrorType.INVALID_ARGUMENT.getMessage() + ":" + "field"
        String expectedMessage = ErrorType.INVALID_ARGUMENT.getMessage() + ":field";
        assertEquals(expectedMessage, response.getMessage());
    }

    @Test
    void testHandleRuntimeException() {
        RuntimeException ex = new RuntimeException("Unexpected");
        ApiErrorResponse response = errorHandler.handleRuntimeException(ex);

        assertNotNull(response);
        assertEquals(ErrorType.UNRESOLVED_ERROR.getMessage(), response.getMessage());
    }

    @Test
    void testHandleException() {
        Exception ex = new Exception("Unexpected");
        ApiErrorResponse response = errorHandler.handleException(ex);

        assertNotNull(response);
        assertEquals(ErrorType.UNRESOLVED_ERROR.getMessage(), response.getMessage());
    }
}
