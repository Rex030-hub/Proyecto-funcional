package com.bibliography.analyzer.dto;

public class Evidencia {
    private String type;
    private String label;
    private String text;
    private String reason;

    public Evidencia() {}

    public Evidencia(String type, String label, String text, String reason) {
        this.type = type;
        this.label = label;
        this.text = text;
        this.reason = reason;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}