package app.insightfuleye.client.models;

import com.google.gson.annotations.SerializedName;

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
}
