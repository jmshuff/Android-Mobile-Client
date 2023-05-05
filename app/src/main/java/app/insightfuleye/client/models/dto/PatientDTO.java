
package app.insightfuleye.client.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PatientDTO {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("visilant_id")
    @Expose
    private String visilantId;
    @SerializedName("first_name")
    @Expose
    private String firstname;
    @SerializedName("middle_name")
    @Expose
    private String middlename;
    @SerializedName("last_name")
    @Expose
    private String lastname;
    @SerializedName("date_of_birth")
    @Expose
    private String dateofbirth;
    @SerializedName("phone_number")
    @Expose
    private String phonenumber;
    @SerializedName("address2")
    @Expose
    private String address2;
    @SerializedName("address1")
    @Expose
    private String address1;
    @SerializedName("city_village")
    @Expose
    private String cityvillage;
    @SerializedName("state_province")
    @Expose
    private String stateprovince;
    @SerializedName("postal_code")
    @Expose
    private String postalcode;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("education")
    @Expose
    private String education;
    @SerializedName("economic")
    @Expose
    private String economic;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("abha_number")
    @Expose
    private String abhaNumber;
    @SerializedName("location_id")
    @Expose
    private String locationId;
    @SerializedName("patient_identifier")
    @Expose
    private String patientIdentifier;
    @SerializedName("patient_identifier_type")
    @Expose
    private String patientIdentiferType;

    private String healthScheme;

    private List<PatientAttributesDTO> patientAttributesDTOList;

    @SerializedName("syncd")
    @Expose
    private Boolean syncd;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getVisilantId() {
        return visilantId;
    }

    public void setVisilantId(String visilantId) {
        this.visilantId = visilantId;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getDateofbirth() {
        return dateofbirth;
    }

    public void setDateofbirth(String dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getCityvillage() {
        return cityvillage;
    }

    public void setCityvillage(String cityvillage) {
        this.cityvillage = cityvillage;
    }

    public String getStateprovince() {
        return stateprovince;
    }

    public void setStateprovince(String stateprovince) {
        this.stateprovince = stateprovince;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    public Boolean getSyncd() {
        return syncd;
    }

    public void setSyncd(Boolean syncd) {
        this.syncd = syncd;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getEconomic() {
        return economic;
    }

    public void setEconomic(String economic) {
        this.economic = economic;
    }


    public List<PatientAttributesDTO> getPatientAttributesDTOList() {
        return patientAttributesDTOList;
    }

    public void setPatientAttributesDTOList(List<PatientAttributesDTO> patientAttributesDTOList) {
        this.patientAttributesDTOList = patientAttributesDTOList;
    }

    public String getAbhaNumber() {
        return abhaNumber;
    }

    public void setAbhaNumber(String abhaNumber) {
        this.abhaNumber = abhaNumber;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public String getPatientIdentiferType() {
        return patientIdentiferType;
    }

    public void setPatientIdentiferType(String patientIdentiferType) {
        this.patientIdentiferType = patientIdentiferType;
    }

    public String getHealthScheme() {
        return healthScheme;
    }

    public void setHealthScheme(String healthScheme) {
        this.healthScheme = healthScheme;
    }
}