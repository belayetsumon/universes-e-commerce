package com.ecommerce.app.module.fraud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FraudCaseAssignRequest {

    @NotBlank(message = "Investigator is required.")
    @Size(max = 120, message = "Investigator must be 120 characters or less.")
    private String investigator;

    @Size(max = 500, message = "Assignment note must be 500 characters or less.")
    private String note;

    public String getInvestigator() { return investigator; }
    public void setInvestigator(String investigator) { this.investigator = investigator; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
