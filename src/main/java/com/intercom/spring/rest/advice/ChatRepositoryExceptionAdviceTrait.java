package com.intercom.spring.rest.advice;

import com.intercom.spring.domain.exception.ChatRepositoryException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.AdviceTrait;

public interface ChatRepositoryExceptionAdviceTrait extends AdviceTrait {
  @ExceptionHandler
  default ResponseEntity<Problem> handleChatRepositoryException(
      final ChatRepositoryException exception, final NativeWebRequest request) {
    return create(Status.BAD_REQUEST, exception, request);
  }
}

