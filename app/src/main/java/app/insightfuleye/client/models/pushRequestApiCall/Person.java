
package app.insightfuleye.client.models.pushRequestApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Person {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @SerializedName("middle_name")
    @Expose
    private String middleName;
    @SerializedName("last_name")
    @Expose
    private String lastName;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("birthdate")
    @Expose
    private String birthdate;
    @SerializedName("location_id")
    @Expose
    private String locationId;
    @SerializedName("person_type_id")
    @Expose
    private String personTypeId;
    @SerializedName("patient")
    @Expose
    private Patient patient;
    @SerializedName("person_address")
    @Expose
    private Address address;
    @SerializedName("creator_id")
    @Expose
    private String creatorId;

    /**
     * No args constructor for use in serialization
     *
     */
    public Person() {
    }

    /**
     *
     * @param firstName
     * @param lastName
     * @param birthdate
     * @param personTypeId
     * @param gender
     * @param locationId
     * @param patient
     * @param middleName
     * @param id
     */
    public Person(String id, String firstName, String middleName, String lastName, String gender, String birthdate, String locationId, String personTypeId, String creatorId, Patient patient) {
        super();
        this.id = id;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthdate = birthdate;
        this.locationId = locationId;
        this.personTypeId = personTypeId;
        this.patient = patient;
        this.creatorId=creatorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getPersonTypeId() {
        return personTypeId;
    }

    public void setPersonTypeId(String personTypeId) {
        this.personTypeId = personTypeId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
}
