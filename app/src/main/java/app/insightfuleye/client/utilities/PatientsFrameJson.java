package app.insightfuleye.client.utilities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;

import app.insightfuleye.client.app.AppConstants;
import app.insightfuleye.client.app.IntelehealthApplication;
import app.insightfuleye.client.database.dao.EncounterDAO;
import app.insightfuleye.client.database.dao.ObsDAO;
import app.insightfuleye.client.database.dao.PatientsDAO;
import app.insightfuleye.client.database.dao.VisitsDAO;
import app.insightfuleye.client.models.dto.EncounterDTO;
import app.insightfuleye.client.models.dto.ObsDTO;
import app.insightfuleye.client.models.dto.PatientDTO;
import app.insightfuleye.client.models.dto.VisitDTO;
import app.insightfuleye.client.models.pushRequestApiCall.Address;
import app.insightfuleye.client.models.pushRequestApiCall.Attribute;
import app.insightfuleye.client.models.pushRequestApiCall.Encounter;
import app.insightfuleye.client.models.pushRequestApiCall.Ob;
import app.insightfuleye.client.models.pushRequestApiCall.ObsString;
import app.insightfuleye.client.models.pushRequestApiCall.Patient;
import app.insightfuleye.client.models.pushRequestApiCall.Person;
import app.insightfuleye.client.models.pushRequestApiCall.Visit;
import app.insightfuleye.client.utilities.exception.DAOException;

public class PatientsFrameJson {
    private PatientsDAO patientsDAO = new PatientsDAO();
    private SessionManager session;
    private VisitsDAO visitsDAO = new VisitsDAO();
    private EncounterDAO encounterDAO = new EncounterDAO();
    private ObsDAO obsDAO = new ObsDAO();
    UuidGenerator uuidGenerator = new UuidGenerator();

/*    public PushRequestApiCall frameJson() {
        session = new SessionManager(IntelehealthApplication.getAppContext());

        //PushRequestApiCall pushRequestApiCall = new PushRequestApiCall();

        List<PatientDTO> patientDTOList = null;
        try {
            patientDTOList = patientsDAO.unsyncedPatients();
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        List<VisitDTO> visitDTOList = visitsDAO.unsyncedVisits();
        List<EncounterDTO> encounterDTOList = encounterDAO.unsyncedEncounters();
        List<Patient> patientList = new ArrayList<>();
        List<Person> personList = new ArrayList<>();
        List<Visit> visitList = new ArrayList<>();
        List<Encounter> encounterList = new ArrayList<>();

        if (patientDTOList != null) {
            for (int i = 0; i < patientDTOList.size(); i++) {

                Person person = new Person();
                person.setBirthdate(patientDTOList.get(i).getDateofbirth());
                person.setGender(patientDTOList.get(i).getGender());
                person.setUuid(patientDTOList.get(i).getUuid());
                person.setFirstName(patientDTOList.get(i).getFirstname());
                person.setLastName(patientDTOList.get(i).getLastname());
                person.setLocationId(session.getLocationUuid());
                person.setPersonTypeId("52deed97-364d-4ba3-8faf-7673d89f235a");
                personList.add(person);

                List<Address> addressList = new ArrayList<>();
                Address address = new Address();
                address.setAddress1(patientDTOList.get(i).getAddress1());
                address.setAddress2(patientDTOList.get(i).getAddress2());
                address.setCityVillage(patientDTOList.get(i).getCityvillage());
                address.setCountry(patientDTOList.get(i).getCountry());
                address.setPostalCode(patientDTOList.get(i).getPostalcode());
                address.setStateProvince(patientDTOList.get(i).getStateprovince());
                addressList.add(address);

                List<Attribute> attributeList = new ArrayList<>();
                attributeList.clear();
                try {
                    attributeList = patientsDAO.getPatientAttributes(patientDTOList.get(i).getUuid());
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                person.setAddresses(addressList);
                person.setAttributes(attributeList);
                Patient patient = new Patient();

                patient.setPersonId(patientDTOList.get(i).getUuid());
                patient.setAbhaNo(patientDTOList.get(i).getAbhaNumber());
                patient.setPatientIdentifier(patientDTOList.get(i).getPatientIdentifier());
                patient.setPatientIdentifierType(patientDTOList.get(i).getPatientIdentiferType());
                patientList.add(patient);
            }
        }
        for (VisitDTO visitDTO : visitDTOList) {
            Visit visit = new Visit();
            visit.setLocation(visitDTO.getLocationuuid());
            visit.setPatient(visitDTO.getPatientuuid());
            visit.setUuid(visitDTO.getUuid());
            visit.setPatient(visitDTO.getPatientuuid());
            //visit.setStartDatetime(visitDTO.getStartdate());
            //visit.setVisitType(visitDTO.getVisitTypeUuid());
            //visit.setStopDatetime(visitDTO.getEnddate());
            //visit.setAttributes(visitDTO.getAttributes());
            visitList.add(visit);

//            if (visitDTO.getAttributes().size() > 0) {
//                visitList.add(visit);
//            }

        }

        for (EncounterDTO encounterDTO : encounterDTOList) {
            Encounter encounter = new Encounter();

            encounter.setUuid(encounterDTO.getUuid());
            //encounter.setEncounterDatetime(encounterDTO.getEncounterTime());//visit start time
            encounter.setEncounterType(encounterDTO.getEncounterTypeUuid());//right know it is static
            encounter.setPatient(visitsDAO.patientUuidByViistUuid(encounterDTO.getVisituuid()));
            encounter.setVisit(encounterDTO.getVisituuid());
            encounter.setEncounterProviders(encounterDTO.getProvideruuid());
            encounter.setLocation(session.getLocationUuid());

            if (!encounterDTO.getEncounterTypeUuid().equalsIgnoreCase(UuidDictionary.EMERGENCY)) {
                List<Ob> obsList = new ArrayList<>();
                List<ObsDTO> obsDTOList = obsDAO.obsDTOList(encounterDTO.getUuid());
                Ob ob = new Ob();
                for (ObsDTO obs : obsDTOList) {
                    if (obs != null && obs.getValue() != null) {
                        if (!obs.getValue().isEmpty()) {
                            ob = new Ob();
                            //Do not set obs uuid in case of emergency encounter type .Some error occuring in open MRS if passed

                            ob.setUuid(obs.getUuid());
                            ob.setConcept(obs.getConceptuuid());
                            ob.setValue(obs.getValue());
                            obsList.add(ob);

                        }
                    }
                }
                encounter.setObs(obsList);
            }


//          encounterList.add(encounter);

            encounterList.add(encounter);

        }


        pushRequestApiCall.setPatients(patientList);
        pushRequestApiCall.setPersons(personList);
        pushRequestApiCall.setVisits(visitList);
        pushRequestApiCall.setEncounters(encounterList);
        Gson gson = new Gson();
        Log.d("OBS: ","OBS: "+gson.toJson(pushRequestApiCall));


        return pushRequestApiCall;
    }*/

    public ArrayList<Person> frameJsonPerson() {
        session = new SessionManager(IntelehealthApplication.getAppContext());
        ArrayList<Person> personRequestCallApi = new ArrayList<>();
        List<PatientDTO> patientDTOList = null;
        try {
            patientDTOList = patientsDAO.unsyncedPatients();
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        if (patientDTOList != null) {
            for (int i = 0; i < patientDTOList.size(); i++) {

                Person person = new Person();
                person.setBirthdate(patientDTOList.get(i).getDateofbirth());
                person.setGender(patientDTOList.get(i).getGender());
                person.setId(generateUuid()); //random uuid for person
                if(patientDTOList.get(i)!=null){
                    person.setFirstName(patientDTOList.get(i).getFirstname());
                }
                else{
                    person.setFirstName("");
                }
                if(patientDTOList.get(i)!=null){
                    person.setLastName(patientDTOList.get(i).getLastname());
                }
                else{
                    person.setLastName("");
                }
                person.setLocationId(session.getLocationUuid());
                person.setPersonTypeId("52deed97-364d-4ba3-8faf-7673d89f235a");
                person.setCreatorId(patientDTOList.get(i).getCreatoruuid());

                Address address = new Address();
                address.setAddress1(patientDTOList.get(i).getAddress1());
                address.setAddress2(patientDTOList.get(i).getAddress2());
                address.setCityVillage(patientDTOList.get(i).getCityvillage());
                address.setCountry(patientDTOList.get(i).getCountry());
                address.setPostalCode(patientDTOList.get(i).getPostalcode());
                address.setStateProvince(patientDTOList.get(i).getStateprovince());
                //person.setAddress(address);

                List<Attribute> attributeList = new ArrayList<>();
                attributeList.clear();
                try {
                    attributeList = patientsDAO.getPatientAttributes(patientDTOList.get(i).getUuid());
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                //person.setAttributes(attributeList);
                //TODO add patient attributes
                Patient patient = new Patient();

                patient.setId(patientDTOList.get(i).getUuid()); //patientId comes from table
//                patient.setAbhaNo(patientDTOList.get(i).getAbhaNumber());
//                patient.setPatientIdentifier(patientDTOList.get(i).getPatientIdentifier());
//                patient.setPatientIdentifierTypeId(patientDTOList.get(i).getPatientIdentiferType());
                patient.setAbhaNo(patientDTOList.get(i).getAbhaNumber());
                patient.setPatientIdentifier(patientDTOList.get(i).getPatientIdentifier());
                patient.setPatientIdentifierTypeId(patientDTOList.get(i).getPatientIdentiferType());
                patient.setCreatoruuid(patientDTOList.get(i).getCreatoruuid());
                person.setPatient(patient);

                personRequestCallApi.add(person);
            }
        }
        return personRequestCallApi;
    }

    public ArrayList<Visit> frameJsonVisit() {
        session = new SessionManager(IntelehealthApplication.getAppContext());
        ArrayList<Visit> visitRequestCallApi = new ArrayList<>();

        List<VisitDTO> visitDTOList = null;
        visitDTOList = visitsDAO.unsyncedVisits();

        //get unsynced visits
        //get unsynced encounters. If encounter is already in visit list, skip
        //get unsynced obs. If ob is already in visit list, skip

        for (VisitDTO visitDTO : visitDTOList) {
            Visit visit = new Visit();
            visit.setLocationId(visitDTO.getLocationuuid());
            visit.setPatientId(visitDTO.getPatientuuid());
            visit.setId(visitDTO.getUuid());
            visit.setCreatorId(visitDTO.getCreatoruuid());
            visit.setVisitTypeId(visitDTO.getVisitTypeUuid());
            //visit.setStartDatetime(visitDTO.getStartdate());
            //visit.setVisitType(visitDTO.getVisitTypeUuid());
            //visit.setStopDatetime(visitDTO.getEnddate());
            //visit.setAttributes(visitDTO.getAttributes());

//            if (visitDTO.getAttributes().size() > 0) {
//                visitList.add(visit);
//            }
            visitRequestCallApi.add(visit);

        }
        return visitRequestCallApi;
    }

    public ArrayList<Encounter> frameJsonEncounter(){
        session = new SessionManager(IntelehealthApplication.getAppContext());
        ArrayList<Encounter> encounterRequestCallApi = new ArrayList<>();

        List<EncounterDTO> encounterDTOList = null;
        encounterDTOList = encounterDAO.unsyncedEncounters();


        for (EncounterDTO encounterDTO : encounterDTOList) {
            Encounter encounter = new Encounter();

            encounter.setId(encounterDTO.getUuid());
            //encounter.setEncounterDatetime(encounterDTO.getEncounterTime());//visit start time
            encounter.setEncounterTypeId(encounterDTO.getEncounterTypeUuid());//right know it is static
            encounter.setPatientId(encounterDTO.getPatientuuid());
            encounter.setVisitId(encounterDTO.getVisituuid());
            encounter.setCreatorId(encounterDTO.getCreatoruuid());

            if (!encounterDTO.getEncounterTypeUuid().equalsIgnoreCase(UuidDictionary.EMERGENCY)) {
                List<Ob> obsList = new ArrayList<>();
                List<ObsDTO> obsDTOList = obsDAO.obsDTOList(encounterDTO.getUuid());
                for (ObsDTO obs : obsDTOList) {
                    if (obs != null && obs.getValue() != null) {
                        if (!obs.getValue().isEmpty()) {
                            Ob ob = new Ob();
                            ObsString obsString = new ObsString();
                            //Do not set obs uuid in case of emergency encounter type .Some error occuring in open MRS if passed

                            ob.setId(obs.getUuid());
                            ob.setConceptId(obs.getConceptuuid());
                            ob.setVisitId(encounterDTO.getVisituuid());
                            ob.setPatientId(encounterDTO.getPatientuuid());
                            ob.setConceptId(obs.getConceptuuid());
                            ob.setCreatorId(encounterDTO.getCreatoruuid());
                            obsString.setValue(obs.getValue());
                            obsList.add(ob);
                        }
                    }
                }
                encounter.setObs(obsList);
            }
            encounterRequestCallApi.add(encounter);

        }
        return encounterRequestCallApi;
    }

    /**
     * @param uuid the visit uuid of the patient visit records is passed to the function.
     * @return boolean value will be returned depending upon if the row exists in the tbl_visit_attribute tbl
     */
    private boolean speciality_row_exist_check(String uuid) {
        boolean isExists = false;
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery("SELECT * FROM tbl_visit_attribute WHERE visit_uuid=?",
                new String[]{uuid});

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                isExists = true;
            }
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return isExists;
    }

    public String generateUuid() {
        String uuid = uuidGenerator.UuidGenerator();
        return uuid;
    }
}
