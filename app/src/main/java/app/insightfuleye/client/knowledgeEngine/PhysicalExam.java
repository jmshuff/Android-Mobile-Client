package app.insightfuleye.client.knowledgeEngine;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Physical Exam information class
 * The creation of this class was so that the original physical exam engine can be modified for each specific use of it.
 */

/**
 * Created by Amal Afroz Alam on 28, April, 2016.
 * Contact me: contact@amal.io
 */
public class PhysicalExam extends Node {

    private static final String TAG = PhysicalExam.class.getSimpleName();

    private ArrayList<String> selection;
    private List<Node> selectedNodes;
    private int totalExams;
    private List<String> pageTitles;
    private String VARight;
    private String VALeft;
    private String PinholeRight;
    private String PinholeLeft;
    private String volunteerReferral;
    private String volunteerReferralLocation;
    private String volunteerDiagnosisRight;
    private String volunteerDiagnosisLeft;


    public PhysicalExam(JSONObject jsonObject, ArrayList<String> selection) {
        super(jsonObject);
        this.selection = selection;
        this.selectedNodes = matchSelections();
        this.totalExams = calculateTotal();
        this.pageTitles = determineTitles();

    }

    public PhysicalExam(JSONObject jsonObject) {
        super(jsonObject);
    }

    public void refresh(ArrayList<String> selection) {
        this.selection = selection;
        this.selectedNodes = matchSelections();
        this.totalExams = calculateTotal();
        this.pageTitles = determineTitles();
    }

    /**
     * When this object is first created, the constructor requires an input string of the exams for the current patient.
     * These exams are located first, and a copy of the original mind map is created using only those required exams.
     * If no exams are selected, the general exams are triggered instead.
     * Currently, exams are stored as follows:
     * Location Node 1 {
     * Exam Node 1 {
     * Question Node 1.1
     * Question Node 1.2
     * Question Node 1.3
     * }
     * }
     */
    private List<Node> matchSelections() {
        List<Node> newOptionsList = new ArrayList<>();
        List<String> foundLocations = new ArrayList<>();
        //Add the general ones into here first
        newOptionsList.add(getOption(0));
        getOption(0).setRequired(true);
        foundLocations.add(newOptionsList.get(0).getText());

        //TODO: Physical exam mind map needs to be modified to include required attribute
        if (getOption(0).getOptionsList() != null) {
            for (int i = 0; i < getOption(0).getOptionsList().size(); i++) {
                getOption(0).getOption(i).setRequired(true);
                if (getOption(0).getOption(i).getOptionsList() != null) {
                    for (int j = 0; j < getOption(0).getOption(i).getOptionsList().size(); j++) {
                        getOption(0).getOption(i).getOption(j).setRequired(true);
                    }
                }
            }




            //Find the other exams that need to be conducted and add them in
            if (selection == null || selection.isEmpty()) {
                //If no exams were required, just do the general ones
                return newOptionsList;
            }
            else {
                for (String current : selection) {
                    if (!current.trim().isEmpty()) {
                /*
                First, the selection texts are taken individually, and split up into location:exam
                The location knowledgeEngine is identified first, and then the exam nodes
                 */

                        if (current != null && !current.isEmpty()) {

                            String[] split = current.split(":");
                            if (split.length > 1) {
                                String location = split[0];
                                String exam = split[1];
                                if (location != null && !location.isEmpty() && exam != null && !exam.isEmpty()) {
                                    Node locationNodeRef = null;

                                    locationNodeRef = getOptionByName(location);

                                    Node examNodeRef = null;
                                    if (locationNodeRef != null) {
                                        Log.i(TAG, "matchSelections: [Location]" + location);
                                        examNodeRef = locationNodeRef.getOptionByName(exam);
                                    }
                                    if (examNodeRef != null) {


                                        //The foundLocation list is to ensure that the same exam isn't display twice
                                        if (foundLocations.contains(location)) {
                                            int locationIndex = foundLocations.indexOf(location);
                                            Node foundLocationNode = newOptionsList.get(locationIndex);
                                            foundLocationNode.addOptions(new Node(examNodeRef));
                                        } else {
                                            //If it's a new exam, the location needs to be added to the list of things to check
                                            foundLocations.add(location);
                                            Node locationNode = new Node(locationNodeRef);
                                            locationNode.removeOptionsList();
                                            locationNode.addOptions(new Node(examNodeRef));
                                            newOptionsList.add(locationNode);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return newOptionsList;
    }

    public List<Node> getSelectedNodes() {
        return selectedNodes;
    }

    private int calculateTotal() {
        int examTotal = 0;
        for (Node node : selectedNodes) {
            for (Node node1 : node.getOptionsList()) {
                examTotal++;
            }
        }

        return examTotal;
    }

    private List<String> determineTitles() {
        List<String> titles = new ArrayList<>();

        for (Node node : selectedNodes) {
            for (Node subNode : node.getOptionsList()) {
                titles.add(node.getText() + " : " + subNode.getText());
            }
        }

        return titles;
    }

    public int getTotalNumberOfExams() {
        return totalExams;
    }

    public List<String> getAllTitles() {
        return pageTitles;
    }

    public String getTitle(int index) {return pageTitles.get(index);
    }

    /**
     * Once the list of exams has been generated, this is used to populate the views for each exam.
     *
     * @param index View number
     * @return Exam for that particular view
     */
    public Node getExamNode(int index) {

        Node lvlTwoNode = null;

        String title = getTitle(index);
        String[] split = title.split(" : ");
        String levelOne = split[0];
        String levelTwo = split[1];
        Log.i(TAG, "title" + title);
        Log.i(TAG, "lvl1: "+ levelOne);
        Log.i(TAG, "lvl2: "+ levelTwo);
        for (Node selectedNode : selectedNodes) {
            if (selectedNode.getText().equals(levelOne)) {
                for (Node node : selectedNode.getOptionsList()) {
                    if (node.getText().equals(levelTwo)) {
                        lvlTwoNode = node;
                    }
                }
            }
        }

        return lvlTwoNode;

    }

    public String getExamParentNodeName(int index) {

        String title = getTitle(index);
        String[] split = title.split(" : ");

        String parent_node = split[0];

        for (Node selectedNode : selectedNodes) {
            if (selectedNode.getText().equals(split[0])) {
                parent_node = selectedNode.findDisplay();
            }
        }
        Log.i(TAG, "parent name"+parent_node);
        return parent_node;

    }

    //Check to see if all required exams have been answered before moving on.
    public boolean areRequiredAnswered() {

        boolean allAnswered = true;

        int total = this.totalExams;
        for (int i = 0; i < total; i++) {
            Node node = getExamNode(i);
            if (node.isRequired() && !node.anySubSelected()) {
                allAnswered = false;
                break;
            }
        }
        return allAnswered;
    }


    //TODO: Physical exam map needs to modified to make language generation easier.
    public String generateFindings() {
        String mLanguage = "";
        Set<String> rootStrings = new HashSet<>();
        List<String> stringsList = new ArrayList<>();

        int total = this.totalExams;
        for (int i = 0; i < total; i++) {
            Node node = getExamNode(i);

            String title = getTitle(i);
            String[] split = title.split(" : ");
            String levelOne = split[0];
            if ((node.isSelected() | node.anySubSelected())) {
                boolean checkSet = rootStrings.add(levelOne);

                if (checkSet)
                    stringsList.add("<b>"+levelOne + ": "+"</b>" + bullet + " " + node.getLanguage());
                else stringsList.add(bullet + " " + node.getLanguage());
                if (!node.isTerminal()) {
                    String lang = node.formLanguage();
                    Log.i(TAG, "generateFindings: "+ lang);
                    stringsList.add(lang);
                }
            }
        }


        String languageSeparator = next_line;

        for (int i = 0; i < stringsList.size(); i++) {
            mLanguage = mLanguage.concat(stringsList.get(i) + languageSeparator);
//            if (i == 0) {
//                if (!stringsList.get(i).isEmpty()) {
//                    mLanguage = mLanguage.concat(stringsList.get(i));
//                }
//            } else {
//                if (!stringsList.get(i).isEmpty()) {
//                    mLanguage = mLanguage.concat(languageSeparator + stringsList.get(i));
//                }
//            }
        }

//        mLanguage = removeCharsFindings(mLanguage);
        mLanguage = mLanguage.replaceAll("\\. -", ".");
        mLanguage = mLanguage.replaceAll("\\.", "\\. ");
        mLanguage = mLanguage.replaceAll("\\: -", "\\: ");
        mLanguage = mLanguage.replaceAll("% - ", "");
        mLanguage = mLanguage.replace(next_line,"-");
        mLanguage = mLanguage.replaceAll("-"+ bullet, next_line + bullet);
        mLanguage = mLanguage.replaceAll("-"+"<b>", next_line +"<b>");
        mLanguage = mLanguage.replaceAll("</b>"+ bullet,"</b>"+ next_line + bullet);

        if(StringUtils.right(mLanguage,2).equals(" -")){
            mLanguage = mLanguage.substring(0,mLanguage.length()-2);
        }

        mLanguage = mLanguage.replaceAll("%-"," ");
        return mLanguage;
    }


    public String generateTable(){

        Set<String> rootStrings = new HashSet<>();
        List<String> stringsList = new ArrayList<>();
        List<String> rightVAList= new ArrayList<>();
        List<String> leftVAList= new ArrayList<>();
        List<String> rightPinList= new ArrayList<>();
        List<String> leftPinList= new ArrayList<>();
        List<String> rightPhysExamList= new ArrayList<>();
        List<String> leftPhysExamList= new ArrayList<>();
        String mRVA = "";
        String mLVA = "";
        String mRPin= "";
        String mLPin="";
        String mRPhys = "";
        String mLPhys= "";
        String mOther="";
        String leftSymptom=getLeftSympt();
        String rightSymptom=getRightSympt();
        String footer=getFooter();


        int total = this.totalExams;
        for (int i = 0; i < total; i++) {
            Node node = getExamNode(i);
            if ((node.isSelected() | node.anySubSelected())) {
                //stringsList.add(bullet + " " + node.getLanguage());
                //Currently not getting any language for
                if (!node.isTerminal()) {
                    String lang = node.formLanguage();
                    Log.i(TAG, "generateFindings: "+ lang);

                    //IF BOTH, it is physical exam, add to both physical exams
                    if (lang.toLowerCase().contains("right")&& lang.toLowerCase().contains("left")){
                        lang=lang.split("-")[0]; //get everything before -
                        rightPhysExamList.add(bullet + " " + lang);
                        leftPhysExamList.add(bullet + " "+ lang);
                    }
                    //If it's not both, check for left, right or neither
                    else {
                        //if right, check for visual acuity, pinhole, or other. All others go in physical exam
                        if (lang.toLowerCase().contains("right")) {
                            if (lang.toLowerCase().contains("visual acuity")) {
                                lang = lang.toLowerCase().replace("right eye", "");
                                lang = lang.toLowerCase().replace("visual acuity:", "");
                                rightVAList.add(bullet + " " + lang); //consider adding node.getLanguage()
                            } else if (lang.toLowerCase().contains("pinhole")) {
                                lang = lang.toLowerCase().replace("right eye", "");
                                lang = lang.toLowerCase().replace("pinhole acuity:", "");
                                rightPinList.add(bullet + " " + lang);
                            } else {
                                lang=lang.split("-")[0]; //get everything before -
                                rightPhysExamList.add(bullet + " " + lang);
                            }
                        }
                        else {
                            if (lang.toLowerCase().contains("left")) {
                                if (lang.toLowerCase().contains("visual acuity")) {
                                    lang = lang.toLowerCase().replace("left eye", "");
                                    lang = lang.toLowerCase().replace("visual acuity:", "");
                                    leftVAList.add(bullet + " " + lang); //consider adding node.getLanguage()
                                } else if (lang.toLowerCase().contains("pinhole")) {
                                    lang = lang.toLowerCase().replace("left eye", "");
                                    lang = lang.toLowerCase().replace("pinhole acuity:", "");
                                    leftPinList.add(bullet + " " + lang);
                                } else {
                                    lang = lang.split("-")[0]; //get everything before -
                                    leftPhysExamList.add(bullet + " " + lang);
                                }

                            } else {
                                if (lang==""){//remove empty lines
                                }
                                else {
                                    stringsList.add(bullet + " " + lang);
                                }

                            }
                        }
                    }

                }
            }
        }

        String languageSeparator = next_line;

        for (int i = 0; i < stringsList.size(); i++) {
            mOther = mOther.concat(stringsList.get(i) + languageSeparator);}

        for (int i=0; i< rightVAList.size(); i++){
            mRVA=mRVA.concat(rightVAList.get(i) + languageSeparator);}

        for (int i=0; i< leftVAList.size(); i++){
            mLVA=mLVA.concat(leftVAList.get(i) + languageSeparator);}

        for (int i=0; i< rightPinList.size(); i++){
            mRPin=mRPin.concat(rightPinList.get(i) + languageSeparator);}

        for (int i=0; i< leftPinList.size(); i++){
            mLPin=mLPin.concat(leftPinList.get(i) + languageSeparator);}

        for (int i=0; i< rightPhysExamList.size(); i++){
            mRPhys = mRPhys.concat(rightPhysExamList.get(i) + languageSeparator);}

        for (int i=0; i< leftPhysExamList.size(); i++){
            mLPhys=mLPhys.concat(leftPhysExamList.get(i) + languageSeparator);}


        mRVA=mRVA.replace(" - ", " ");
        mLVA=mLVA.replace(" - ", " ");
        mRPin=mRPin.replace(" - ", " ");
        mLPin=mLPin.replace(" - ", " ");
        mRPhys=mRPhys.replace(" - ", " ");
        mLPhys=mLPhys.replace(" - ", " ");
        mOther=mOther.replace(" - ", " ");


        Log.i("rightVA", String.valueOf(mRVA));
        Log.i("leftVA", String.valueOf(mLVA));
        Log.i("rightPin", String.valueOf(mRPin));
        Log.i("leftPin", String.valueOf(mLPin));
        Log.i("rightPhys", String.valueOf(mRPhys));
        Log.i("leftPhys", String.valueOf(mLPhys));
        Log.i("stringList", String.valueOf(mOther));

        String mTable;
        mTable="<table>" +
                "<tr>"+
                "<th></th>"+
                "<th>Right Eye</th>"+
                "<th>Left Eye</th>"+
                "</tr>"+
                "<tr>"+
                "<th>Chief Complaint </th>"+
                "<td>"+leftSymptom+"</td>"+
                "<td>"+rightSymptom+"</td>"+
                "</tr>"+
                "<tr>"+
                "<th>Visual Acuity</th>"+
                "<td>"+mRVA+"</td>"+
                "<td>"+mLVA+"</td>"+
                "</tr>"+
                "<tr>"+
                "<th>Pinhole Acuity</th>"+
                "<td>"+mRPin+"</td>"+
                "<td>"+mLPin+"</td>"+
                "</tr>"+
                "<tr>"+
                "<th>Physical Exam</th>"+
                "<td>"+mRPhys+"</td>"+
                "<td>"+mLPhys+"</td>"+
                "<tr>"+
                "</table>"+
                footer+
                mOther;
        return mTable;

    }


    private String removeCharsFindings(String raw) {
        String formatted;
        if (Character.toString(raw.charAt(0)).equals(",")) {
            formatted = raw.substring(2);
        } else {
            formatted = raw;
        }
        return formatted;
    }

    public void setVARight(String VARight){this.VARight=VARight; }
    public void setVALeft(String VALeft){this.VALeft=VALeft; }
    public void setPinholeRight(String PinholeRight){this.PinholeRight=PinholeRight; }
    public void setPinholeLeft(String PinholeLeft){this.PinholeLeft=PinholeLeft; }
    public void setVolunteerReferral(String volunteerReferral){this.volunteerReferral=volunteerReferral; }
    public void setVolunteerReferralLocation(String volunteerReferralLocation){this.volunteerReferralLocation=volunteerReferralLocation; }

    public String getVolunteerDiagnosisRight() {
        return volunteerDiagnosisRight;
    }

    public void setVolunteerDiagnosisRight(String volunteerDiagnosisRight) {
        this.volunteerDiagnosisRight = volunteerDiagnosisRight;
    }

    public String getVolunteerDiagnosisLeft() {
        return volunteerDiagnosisLeft;
    }

    public void setVolunteerDiagnosisLeft(String volunteerDiagnosisLeft) {
        this.volunteerDiagnosisLeft = volunteerDiagnosisLeft;
    }

    public String getVARight(){return VARight;}
    public String getVALeft(){return VALeft;}
    public String getPinholeRight(){return  PinholeRight;}
    public String getPinholeLeft(){return PinholeLeft;}
    public String getVolunteerReferral(){return volunteerReferral;}
    public String getVolunteerReferralLocation(){return volunteerReferralLocation;}




}