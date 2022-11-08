package app.insightfuleye.client.models;

public class FollowUpPatientModel {
    String uuid;
    String patientuuid;
    String startdate;
    String enddate;
    String openmrs_id;
    String first_name;
    String middle_name;
    String last_name;
    String date_of_birth;
    String phone_number;
    String sync;
    String location;
    String rightEyeDiagnosis;
    String leftEyeDiagnosis;

    public String getRightEyeDiagnosis() {
        return rightEyeDiagnosis;
    }

    public void setRightEyeDiagnosis(String rightEyeDiagnosis) {
        this.rightEyeDiagnosis = rightEyeDiagnosis;
    }

    public String getLeftEyeDiagnosis() {
        return leftEyeDiagnosis;
    }

    public void setLeftEyeDiagnosis(String leftEyeDiagnosis) {
        this.leftEyeDiagnosis = leftEyeDiagnosis;
    }

    public FollowUpPatientModel(String uuid, String patientuuid, String startdate, String enddate, String openmrs_id, String first_name, String middle_name, String last_name, String date_of_birth, String phone_number, String sync, String location, String rightEyeDiagnosis, String leftEyeDiagnosis) {
        this.uuid = uuid;
        this.patientuuid = patientuuid;
        this.startdate = startdate;
        this.enddate = enddate;
        this.openmrs_id = openmrs_id;
        this.first_name = first_name;
        this.middle_name = middle_name;
        this.last_name = last_name;
        this.date_of_birth = date_of_birth;
        this.phone_number = phone_number;
        this.sync = sync;
        this.location=location;
        this.rightEyeDiagnosis=rightEyeDiagnosis;
        this.leftEyeDiagnosis=leftEyeDiagnosis;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPatientuuid() {
        return patientuuid;
    }

    public void setPatientuuid(String patientuuid) {
        this.patientuuid = patientuuid;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public String getOpenmrs_id() {
        return openmrs_id;
    }

    public void setOpenmrs_id(String openmrs_id) {
        this.openmrs_id = openmrs_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getSync() {
        return sync;
    }

    public void setSync(String sync) {
        this.sync = sync;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "FollowUpPatientModel{" +
                "uuid='" + uuid + '\'' +
                ", patientuuid='" + patientuuid + '\'' +
                ", startdate='" + startdate + '\'' +
                ", enddate='" + enddate + '\'' +
                ", openmrs_id='" + openmrs_id + '\'' +
                ", first_name='" + first_name + '\'' +
                ", middle_name='" + middle_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", date_of_birth='" + date_of_birth + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", sync='" + sync + '\'' +
                ", location='" + location + '\'' +
                ", rightEyeDiagnosis='" + rightEyeDiagnosis + '\'' +
                ", leftEyeDiagnosis='" + leftEyeDiagnosis + '\'' +
                '}';
    }
}
