
package app.insightfuleye.client.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PatientAttributesDTO {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("patient_attribute_type_id")
    @Expose
    private String personAttributeTypeUuid;
    @SerializedName("patient_uuid")
    @Expose
    private String patientuuid;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPersonAttributeTypeUuid() {
        return personAttributeTypeUuid;
    }

    public void setPersonAttributeTypeUuid(String personAttributeTypeUuid) {
        this.personAttributeTypeUuid = personAttributeTypeUuid;
    }

    public String getPatientuuid() {
        return patientuuid;
    }

    public void setPatientuuid(String patientuuid) {
        this.patientuuid = patientuuid;
    }

}