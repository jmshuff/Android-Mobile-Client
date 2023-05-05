package app.insightfuleye.client.models;

public class hospitalImagingModel {
    private String visitUuid;
    private String patientUuid;
    private String patientIdentifier;
    private String imageName;
    private String encounterAdultInitial;
    private String encounterHospitalImaging;

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getEncounterAdultInitial() {
        return encounterAdultInitial;
    }

    public void setEncounterAdultInitial(String encounterAdultInitial) {
        this.encounterAdultInitial = encounterAdultInitial;
    }

    public String getEncounterHospitalImaging() {
        return encounterHospitalImaging;
    }

    public void setEncounterHospitalImaging(String encounterHospitalImaging) {
        this.encounterHospitalImaging = encounterHospitalImaging;
    }
}
