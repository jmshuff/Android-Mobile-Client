
package app.insightfuleye.client.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationDTO {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("id")
    @Expose
    private String locationuuid;
    @SerializedName("deleted_at")
    @Expose
    private String locationDeleted;
    @SerializedName("created_at")
    @Expose
    private String locationCreated;
    @SerializedName("updated_at")
    private String locationUpdated;
    @SerializedName("created_by")
    @Expose
    private String locationCreatedBy;
    @SerializedName("modified_by")
    @Expose
    private String locationModifiedBy;
    @SerializedName("description")
    @Expose
    private String locationDescription;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocationuuid() {
        return locationuuid;
    }

    public void setLocationuuid(String locationuuid) {
        this.locationuuid = locationuuid;
    }
}