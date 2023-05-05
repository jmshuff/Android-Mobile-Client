package app.insightfuleye.client.models.pushRequestApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ObsString {

    @SerializedName("value")
    @Expose
    private String value;

    /**
     * No args constructor for use in serialization
     *
     */
    public ObsString() {
    }

    /**
     *
     * @param value
     */
    public ObsString(String value) {
        super();
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}