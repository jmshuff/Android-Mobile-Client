
package app.insightfuleye.client.models.pushRequestApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import app.insightfuleye.client.models.dto.VisitAttribute_Speciality;

public class Visit {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("visit_type_id")
    @Expose
    private String visitTypeId;
    @SerializedName("location_id")
    @Expose
    private String locationId;
    @SerializedName("creator_id")
    @Expose
    private String creatorId;
    @SerializedName("encounter")
    @Expose
    private List<Encounter> encounter;
    @SerializedName("patient_id")
    @Expose
    private String patientId;

    /**
     * No args constructor for use in serialization
     *
     */
    public Visit() {
    }

    /**
     *
     * @param locationId
     * @param creatorId
     * @param visitTypeId
     * @param id
     * @param encounter
     */
    public Visit(String id, String visitTypeId, String locationId, String creatorId, List<Encounter> encounter) {
        super();
        this.id = id;
        this.visitTypeId = visitTypeId;
        this.locationId = locationId;
        this.creatorId = creatorId;
        this.encounter = encounter;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVisitTypeId() {
        return visitTypeId;
    }

    public void setVisitTypeId(String visitTypeId) {
        this.visitTypeId = visitTypeId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public List<Encounter> getEncounter() {
        return encounter;
    }

    public void setEncounter(List<Encounter> encounter) {
        this.encounter = encounter;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}