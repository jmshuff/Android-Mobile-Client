package app.insightfuleye.client.models;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class azureResults {
    @SerializedName("id")
    private String ImageId;
    @SerializedName("visit_id")
    private String visitId;
    @SerializedName("patient_id")
    private String patientId;
    @SerializedName("image_path")
    private String imagePath;
    @SerializedName("created_by")
    private String chwName;
    @SerializedName("efficient")
    private String imageQuality;
    @SerializedName("type")
    private String leftRight;
    @SerializedName("created_at")
    private String creationDate;

    private String VARight;
    private String VALeft;
    private String PinholeRight;
    private String PinholeLeft;
    private String age;
    private String sex;
    private String complaints;

    private ArrayList<String> complaintsRight;

    public ArrayList<String> getComplaintsRight() {
        return complaintsRight;
    }

    public void setComplaintsRight(ArrayList<String> complaintsRight) {
        this.complaintsRight = complaintsRight;
    }

    public ArrayList<String> getComplaintsLeft() {
        return complaintsLeft;
    }

    public void setComplaintsLeft(ArrayList<String> complaintsLeft) {
        this.complaintsLeft = complaintsLeft;
    }

    public ArrayList<String> getDiagnosisRight() {
        return diagnosisRight;
    }

    public void setDiagnosisRight(ArrayList<String> diagnosisRight) {
        this.diagnosisRight = diagnosisRight;
    }

    public ArrayList<String> getDiagnosisLeft() {
        return diagnosisLeft;
    }

    public void setDiagnosisLeft(ArrayList<String> diagnosisLeft) {
        this.diagnosisLeft = diagnosisLeft;
    }

    private ArrayList<String> complaintsLeft;
    private ArrayList<String> diagnosisRight;
    private ArrayList<String> diagnosisLeft;

    public String getImageId() {
        return ImageId;
    }


    public void setImageId(String imageId) {
        ImageId = imageId;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getChwName() {
        return chwName;
    }

    public void setChwName(String chwName) {
        this.chwName = chwName;
    }

    public String getImageQuality() {
        return imageQuality;
    }

    public void setImageQuality(String imageQuality) {
        this.imageQuality = imageQuality;
    }

    public String getLeftRight() {
        return leftRight;
    }

    public void setLeftRight(String leftRight) {
        this.leftRight = leftRight;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getVARight() {
        return VARight;
    }

    public void setVARight(String VARight) {
        this.VARight = VARight;
    }

    public String getVALeft() {
        return VALeft;
    }

    public void setVALeft(String VALeft) {
        this.VALeft = VALeft;
    }

    public String getPinholeRight() {
        return PinholeRight;
    }

    public void setPinholeRight(String pinholeRight) {
        PinholeRight = pinholeRight;
    }

    public String getPinholeLeft() {
        return PinholeLeft;
    }

    public void setPinholeLeft(String pinholeLeft) {
        PinholeLeft = pinholeLeft;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getComplaints() {
        return complaints;
    }

    public void setComplaints(String complaints) {
        this.complaints = complaints;
    }

    public String toString(){
        return "PatientID: " + patientId + ", CreatorId: " + chwName + ", Type: " + leftRight + ", VisitID: " + visitId + ", Image Path: " + imagePath + "VARight " + VARight + "VALeft: " + VALeft + "Age: " + age + "Sex: " + sex;
    }

}
