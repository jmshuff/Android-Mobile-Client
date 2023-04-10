
package app.insightfuleye.client.models.pushRequestApiCall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Person {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("first_name")
    @Expose
    private String firstName = null;
    @SerializedName("last_name")
    @Expose
    private String lastName=null;
    @SerializedName("birthdate")
    @Expose
    private String birthdate;
    @SerializedName("person_type_id")
    @Expose
    private String personTypeId;
    @SerializedName("location_id")
    @Expose
    private String locationId;

    @SerializedName("attributes")
    @Expose
    private List<Attribute> attributes = null;
    @SerializedName("addresses")
    @Expose
    private List<Address> addresses = null;
    @Expose
    private List<Name> names=null;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getPersonTypeId() {
        return personTypeId;
    }

    public void setPersonTypeId(String personTypeId) {
        this.personTypeId = personTypeId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    //JS need to edit
    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Name> getNames() {
        return names;
    }

    public void setNames(List<Name> names) {
        this.names = names;
    }
}
