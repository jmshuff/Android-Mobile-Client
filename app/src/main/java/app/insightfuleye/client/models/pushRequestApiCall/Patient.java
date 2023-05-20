
package app.insightfuleye.client.models.pushRequestApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Patient {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("patient_identifier")
    @Expose
    private String patientIdentifier;
    @SerializedName("patient_identifier_type_id")
    @Expose
    private String patientIdentifierTypeId;
    @SerializedName("abha_no")
    @Expose
    private String abhaNo;
    @SerializedName("visits")
    @Expose
    private List<Visit> visits;
    @SerializedName("creator_id")
    @Expose
    private String creatoruuid;


    /**
     * No args constructor for use in serialization
     *
     */
    public Patient() {
    }

    /**
     *
     * @param abhaNo
     * @param visits
     * @param patientIdentifier
     * @param id
     * @param patientIdentifierTypeId
     */
    public Patient(String id, String patientIdentifier, String patientIdentifierTypeId, String abhaNo, String creatoruuid, List<Visit> visits) {
        super();
        this.id = id;
        this.patientIdentifier = patientIdentifier;
        this.patientIdentifierTypeId = patientIdentifierTypeId;
        this.abhaNo = abhaNo;
        this.visits = visits;
        this.creatoruuid = creatoruuid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public String getPatientIdentifierTypeId() {
        return patientIdentifierTypeId;
    }

    public void setPatientIdentifierTypeId(String patientIdentifierTypeId) {
        this.patientIdentifierTypeId = patientIdentifierTypeId;
    }

    public String getAbhaNo() {
        return abhaNo;
    }

    public void setAbhaNo(String abhaNo) {
        this.abhaNo = abhaNo;
    }

    public List<Visit> getVisits() {
        return visits;
    }

    public void setVisits(List<Visit> visits) {
        this.visits = visits;
    }

    public String getCreatoruuid() {
        return creatoruuid;
    }

    public void setCreatoruuid(String creatoruuid) {
        this.creatoruuid = creatoruuid;
    }
}
