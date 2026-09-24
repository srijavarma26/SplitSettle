package com.splitsettle.settlementservice.dto;

public class MarkPaidRequest {
    private String notes;

    public MarkPaidRequest() {
    }

    public MarkPaidRequest(String notes) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
