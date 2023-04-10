package app.insightfuleye.client.models.loginModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Signin {
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("data")
    @Expose
    private Data data;

    /**
     * No args constructor for use in serialization
     */
    public Signin() {
    }

    /**
     * @param date
     * @param data
     * @param success
     * @param version
     */
    public Signin(Boolean success, String version, String date, Data data) {
        super();
        this.success = success;
        this.version = version;
        this.date = date;
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
