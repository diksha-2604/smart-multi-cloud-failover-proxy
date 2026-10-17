
package com.smcfp.gateway;

public class RequestAttempt {

    private String attemptId;
    private String requestId;
    private String timestamp;
    private String completedAt;
    private String path;
    private String backend;
    private int attemptNumber;
    private Integer statusCode;
    private String result;
    private String failureReason;

    public RequestAttempt() {
    }

    public RequestAttempt(
            String attemptId,
            String requestId,
            String timestamp,
            String path,
            String backend,
            int attemptNumber) {

        this.attemptId = attemptId;
        this.requestId = requestId;
        this.timestamp = timestamp;
        this.path = path;
        this.backend = backend;
        this.attemptNumber = attemptNumber;
        this.result = "PROCESSING";
    }

    public String getAttemptId() {
        return attemptId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public String getPath() {
        return path;
    }

    public String getBackend() {
        return backend;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getResult() {
        return result;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    public void setAttemptId(String attemptId) {
        this.attemptId = attemptId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

}
