package com.br.apigenbarcode.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiError {

    private String type;
    private int status;
    private String instance;
    private String title;
    private String detail;
    private List<String> errors;

}