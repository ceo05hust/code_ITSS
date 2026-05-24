package com.aims.aimsbackend.subsystem.vietqr.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class QRRequest {

    protected static final String CONTENT_TYPE  = "application/json";
    protected static final String AUTHORIZATION = "Authorization";

    public abstract String buildRequestString();
}
