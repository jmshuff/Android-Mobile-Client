
package app.insightfuleye.client.models.pushRequestApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ob {

    @SerializedName("encounter_id")
    @Expose
    private String encounterId;
    @SerializedName("patient_id")
    @Expose
    private String patientId;
    @SerializedName("visit_id")
    @Expose
    private String visitId;
    @SerializedName("concept_id")
    @Expose
    private String conceptId;
    @SerializedName("creator_id")
    @Expose
    private String creatorId;
    @SerializedName("obs_string")
    @Expose
    private ObsString obsString;
    @SerializedName("id")
    @Expose
    private String id;

    /**
     * No args constructor for use in serialization
     *
     */
    public Ob() {
    }

    /**
     *
     * @param visitId
     * @param patientId
     * @param obsString
     * @param creatorId
     * @param conceptId
     * @param encounterId
     */
    public Ob(String encounterId, String patientId, String visitId, String conceptId, String creatorId, ObsString obsString) {
        super();
        this.encounterId = encounterId;
        this.patientId = patientId;
        this.visitId = visitId;
        this.conceptId = conceptId;
        this.creatorId = creatorId;
        this.obsString = obsString;
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
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

    public String getConceptId() {
        return conceptId;
    }

    public void setConceptId(String conceptId) {
        this.conceptId = conceptId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public ObsString getObsString() {
        return obsString;
    }

    public void setObsString(ObsString obsString) {
        this.obsString = obsString;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
