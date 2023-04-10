/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package app.insightfuleye.client.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Results<T> implements Serializable {
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
    private app.insightfuleye.client.models.Data data;

    /**
     * No args constructor for use in serialization
     *
     */
    public Results() {
    }

    /**
     *
     * @param date
     * @param data
     * @param success
     * @param version
     */
    public Results(Boolean success, String version, String date, app.insightfuleye.client.models.Data data) {
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

    public app.insightfuleye.client.models.Data getData() {
        return data;
    }

    public void setLocation(Data data) {
        this.data = data;
    }
}