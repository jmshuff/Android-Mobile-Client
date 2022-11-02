package app.insightfuleye.client.activities.patientDetailActivity;

public class followUpVisit {
    String uuid;
    String encounterDate;
    String visitUuid;


    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public followUpVisit(String uuid, String encounterDate, String visitUuid) {
        this.uuid = uuid;
        this.encounterDate = encounterDate;
        this.visitUuid = visitUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(String encounterDate) {
        this.encounterDate = encounterDate;
    }

    @Override
    public String toString() {
        return "followUpVisit{" +
                "uuid='" + uuid + '\'' +
                ", encounterDate='" + encounterDate + '\'' +
                ", visitUuid='" + visitUuid + '\'' +
                '}';
    }
}
