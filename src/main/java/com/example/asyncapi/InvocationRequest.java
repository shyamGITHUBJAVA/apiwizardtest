package com.example.asyncapi;

import lombok.Data;



@Data
public class InvocationRequest {
    private ApiMethod apiMethod;
    private RequestDTO requestDTO;
    private int timeout;
    private boolean useSsl;
}

