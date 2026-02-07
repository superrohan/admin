package com.company.adminbackend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response payload returned by ControllerApp for scan-related operations.
 * JsonIgnoreProperties ensures forward-compatibility if ControllerApp adds new fields.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanResponse {

    private String scanId;
    private String status;
    private String message;

    public ScanResponse() {
    }

    public ScanResponse(String scanId, String status, String message) {
        this.scanId = scanId;
        this.status = status;
        this.message = message;
    }

    public String getScanId() {
        return scanId;
    }

    public void setScanId(String scanId) {
        this.scanId = scanId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
