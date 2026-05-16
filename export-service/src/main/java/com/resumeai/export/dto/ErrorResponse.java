package com.resumeai.export.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int status;

    private String message;

    private LocalDateTime timestamp;

    private String path;

    private Map<String, String> errors;
}
