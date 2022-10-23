package app.insightfuleye.client.models;

public class traumaHist {

    String modeInjury;
    String traumaOccur;
    boolean docConsult;
    boolean temUsed;
    boolean otcUsed;
    String temType;
    String temFreq;
    String otcType;
    String otcFreq;

    public String getModeInjury() {
        return modeInjury;
    }

    public void setModeInjury(String modeInjury) {
        this.modeInjury = modeInjury;
    }

    public String getTraumaOccur() {
        return traumaOccur;
    }

    public void setTraumaOccur(String traumaOccur) {
        this.traumaOccur = traumaOccur;
    }

    public boolean isDocConsult() {
        return docConsult;
    }

    public void setDocConsult(boolean docConsult) {
        this.docConsult = docConsult;
    }

    public boolean isTemUsed() {
        return temUsed;
    }

    public void setTemUsed(boolean temUsed) {
        this.temUsed = temUsed;
    }

    public boolean isOtcUsed() {
        return otcUsed;
    }

    public void setOtcUsed(boolean otcUsed) {
        this.otcUsed = otcUsed;
    }

    public String getTemType() {
        return temType;
    }

    public void setTemType(String temType) {
        this.temType = temType;
    }

    public String getTemFreq() {
        return temFreq;
    }

    public void setTemFreq(String temFreq) {
        this.temFreq = temFreq;
    }

    public String getOtcType() {
        return otcType;
    }

    public void setOtcType(String otcType) {
        this.otcType = otcType;
    }

    public String getOtcFreq() {
        return otcFreq;
    }

    public void setOtcFreq(String otcFreq) {
        this.otcFreq = otcFreq;
    }
}
