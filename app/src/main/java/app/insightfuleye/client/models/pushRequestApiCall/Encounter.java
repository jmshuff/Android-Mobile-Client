
package app.insightfuleye.client.models.pushRequestApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Encounter {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("encounter_type_id")
    @Expose
    private String encounterTypeId;
    @SerializedName("creator_id")
    @Expose
    private String creatorId;
    @SerializedName("patient_id")
    @Expose
    private String patientId;
    @SerializedName("visit_id")
    @Expose
    private String visitId;
    @SerializedName("obs")
    @Expose
    private List<Ob> obs;

    /**
     * No args constructor for use in serialization
     *
     */
    public Encounter() {
    }

    /**
     *
     * @param encounterTypeId
     * @param obs
     * @param visitId
     * @param patientId
     * @param creatorId
     * @param id
     */
    public Encounter(String id, String encounterTypeId, String creatorId, String patientId, String visitId, List<Ob> obs) {
        super();
        this.id = id;
        this.encounterTypeId = encounterTypeId;
        this.creatorId = creatorId;
        this.patientId = patientId;
        this.visitId = visitId;
        this.obs = obs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEncounterTypeId() {
        return encounterTypeId;
    }

    public void setEncounterTypeId(String encounterTypeId) {
        this.encounterTypeId = encounterTypeId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public List<Ob> getObs() {
        return obs;
    }

    public void setObs(List<Ob> obs) {
        this.obs = obs;
    }

}