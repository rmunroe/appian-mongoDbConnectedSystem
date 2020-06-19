package com.appiancorp.solutionsconsulting.cs.mongodb;

import com.appian.connectedsystems.templateframework.sdk.IntegrationError;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;

import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("unused")
public class ConnectedSystemUtil {
    private Long startTime;
    private Long endTime;
    private final HashMap<String, Object> requestDiagnostics;
    private final HashMap<String, Object> responseDiagnostics;
    private final HashMap<String, Object> response;

    public ConnectedSystemUtil(String method) {
        requestDiagnostics = new HashMap<>();
        responseDiagnostics = new HashMap<>();
        responseDiagnostics.put("MongoDB Java Driver API", "org.mongodb:mongodb-driver-sync:3.12.5");
        responseDiagnostics.put("Method Used", method);
        response = new HashMap<>();
    }


    public void startTiming() {
        startTime = System.currentTimeMillis();
    }

    public void stopTiming() {
        endTime = System.currentTimeMillis();
    }

    public long getTiming() {
        if (startTime == null || endTime == null) {
            System.out.println("Start or End was never called, timing is invalid");
            return -1;
        }
        return endTime - startTime;
    }

    public void addRequestDiagnostic(String key, Object value) {
        requestDiagnostics.put(key, value);
    }

    public void addAllRequestDiagnostic(Map<String, Object> map) {
        requestDiagnostics.putAll(map);
    }

    public HashMap<String, Object> getRequestDiagnostics() {
        return requestDiagnostics;
    }


    public void addResponseDiagnostic(String key, Object value) {
        responseDiagnostics.put(key, value);
    }

    public void addAllResponseDiagnostic(Map<String, Object> map) {
        responseDiagnostics.putAll(map);
    }

    public HashMap<String, Object> getResponseDiagnostics() {
        return responseDiagnostics;
    }

    public void addResponse(String key, Object value) {
        response.put(key, value);
    }

    public void addAllResponse(Map<String, Object> map) {
        response.putAll(map);
    }

    public IntegrationResponse buildSuccess() {
        IntegrationResponse.Builder integrationResponseBuilder = IntegrationResponse.forSuccess(getResponse());
        IntegrationDesignerDiagnostic integrationDesignerDiagnostic = IntegrationDesignerDiagnostic.builder()
                .addRequestDiagnostic(getRequestDiagnostics())
                .addResponseDiagnostic(getResponseDiagnostics())
                .addExecutionTimeDiagnostic(getTiming())
                .build();
        return integrationResponseBuilder.withDiagnostic(integrationDesignerDiagnostic).build();
    }

    public HashMap<String, Object> getResponse() {
        return response;
    }


    public IntegrationResponse buildApiExceptionError(String detail) {
        return buildApiExceptionError(
                "Could not perform action",
                "Could not perform requested action, verify that the inputs are valid.",
                detail
        );
    }

    public IntegrationResponse buildApiExceptionError(String message, String detail) {
        return buildApiExceptionError(
                "Could not perform action",
                message,
                detail
        );
    }

    public IntegrationResponse buildApiExceptionError(String title, String message, String detail) {
        IntegrationDesignerDiagnostic diagnostics = IntegrationDesignerDiagnostic.builder()
                .addRequestDiagnostic(requestDiagnostics)
                .addResponseDiagnostic(responseDiagnostics)
                .build();
        IntegrationError integrationError = IntegrationError.builder()
                .title(title)
                .message(message)
                .detail(detail)
                .build();
        IntegrationResponse.Builder integrationResponseBuilder = IntegrationResponse.forError(integrationError).withDiagnostic(diagnostics);
        return integrationResponseBuilder.build();
    }
}
