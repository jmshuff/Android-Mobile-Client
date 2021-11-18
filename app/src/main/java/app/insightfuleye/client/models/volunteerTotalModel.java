package app.insightfuleye.client.models;

public class volunteerTotalModel {
    private String volunteerName;
    private int volunteerTotal;

    public volunteerTotalModel(String volunteerName, int volunteerTotal) {
        this.volunteerName = volunteerName;
        this.volunteerTotal = volunteerTotal;
    }

    public String getVolunteerName() {
        return volunteerName;
    }

    public void setVolunteerName(String volunteerName) {
        this.volunteerName = volunteerName;
    }

    public int getVolunteerTotal() {
        return volunteerTotal;
    }

    public void setVolunteerTotal(int volunteerTotal) {
        this.volunteerTotal = volunteerTotal;
    }
}
