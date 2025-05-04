package com.example.asyncapi;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class RequestDTO {
    private String url;
    private Map<String, String> headerVariables;
    private List<Param> params;
    private String bodyType;
    private String requestBody;
}


