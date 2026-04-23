package com.hospital.model;

public class PrescriptionItem {
    private int id;
    private int prescriptionId;
    private int drugId;
    private String drugName;
    private String dosage;
    private String frequency;
    private String duration;
    private String instructions;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(int prescriptionId) { this.prescriptionId = prescriptionId; }
    public int getDrugId() { return drugId; }
    public void setDrugId(int drugId) { this.drugId = drugId; }
    public String getDrugName() { return drugName; }
    public void setDrugName(String drugName) { this.drugName = drugName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}
