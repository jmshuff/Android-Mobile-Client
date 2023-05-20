
package app.insightfuleye.client.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EncounterDTO {
    @SerializedName("id")
    @Expose
    private String uuid;
    @SerializedName("patient_id")
    @Expose
    private String patientuuid;
    @SerializedName("visit_id")
    @Expose
    private String visituuid;
    @SerializedName("encounter_type_id")
    @Expose
    private String encounterTypeUuid;
    @SerializedName("encounter_time")
    @Expose
    private String encounterTime;
    @SerializedName("provider_id")
    @Expose
    private String creatoruuid;
    @SerializedName("syncd")
    @Expose
    private Boolean syncd;
    @SerializedName("voided")
    @Expose
    private Integer voided;
    @SerializedName("privacynotice_value")
    @Expose
    private String privacynotice_value;



    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getVisituuid() {
        return visituuid;
    }

    public void setVisituuid(String visituuid) {
        this.visituuid = visituuid;
    }

    public String getEncounterTypeUuid() {
        return encounterTypeUuid;
    }

    public void setEncounterTypeUuid(String encounterTypeUuid) {
        this.encounterTypeUuid = encounterTypeUuid;
    }

    public Boolean getSyncd() {
        return syncd;
    }

    public void setSyncd(Boolean syncd) {
        this.syncd = syncd;
    }

    public Integer getVoided() {
        return voided;
    }

    public void setVoided(Integer voided) {
        this.voided = voided;
    }

    public String getEncounterTime() {
        return encounterTime;
    }

    public void setEncounterTime(String encounterTime) {
        this.encounterTime = encounterTime;
    }

    public String getCreatoruuid() {
        return creatoruuid;
    }

    public void setCreatoruuid(String creatoruuid) {
        this.creatoruuid = creatoruuid;
    }

    public String getPrivacynotice_value() {
        return privacynotice_value;
    }

    public void setPrivacynotice_value(String privacynotice_value) {
        this.privacynotice_value = privacynotice_value;
    }

    public String getPatientuuid() {
        return patientuuid;
    }

    public void setPatientuuid(String patientuuid) {
        this.patientuuid = patientuuid;
    }
}