package app.insightfulsceh.client.models;

import com.google.gson.annotations.SerializedName;

public class azureResultsPush {

    @SerializedName("visitId")
    private String visitId;
    @SerializedName("patientId")
    private String patientId;
    @SerializedName("creatorId")
    private String chwName;
    @SerializedName("type")
    private String leftRight;


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

    public String getChwName() {
        return chwName;
    }

    public void setChwName(String chwName) {
        this.chwName = chwName;
    }


    public String getLeftRight() {
        return leftRight;
    }

    public void setLeftRight(String leftRight) {
        this.leftRight = leftRight;
    }


    public String toString(){
        return "PatientID: " + patientId + ", CreatorId: " + chwName + ", Type: " + leftRight + ", VisitID: " + visitId ;
    }
}
