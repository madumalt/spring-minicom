package com.intercom.spring.rest.advice;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.web.advice.ProblemHandling;

/**
 * Global exception handler — composes all AdviceTrait interfaces.
 * Zalando ProblemHandling provides base Spring exception translation.
 */
@ControllerAdvice
public class ExceptionHandlerAdvice implements ProblemHandling,
    UserNotFoundExceptionAdviceTrait,
    ChatRepositoryExceptionAdviceTrait,
    InvalidInputExceptionAdviceTrait {
}

