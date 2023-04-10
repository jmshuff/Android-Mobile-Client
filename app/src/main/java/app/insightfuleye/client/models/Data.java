package app.insightfuleye.client.models;

import android.location.Location;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import app.insightfuleye.client.models.dto.LocationDTO;

public class Data<T> {
    @SerializedName("rows")
    @Expose
    private List<LocationDTO> rows;
    @SerializedName("total")
    @Expose
    private Integer total;

    /**
     * No args constructor for use in serialization
     *
     */
    public Data() {
    }

    /**
     *
     * @param total
     * @param rows
     */
    public Data(List<LocationDTO> rows, Integer total) {
        super();
        this.rows = rows;
        this.total = total;
    }

    public List<LocationDTO> getRows() {
        return rows;
    }

    public void setRows(List<LocationDTO> rows) {
        this.rows = rows;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}