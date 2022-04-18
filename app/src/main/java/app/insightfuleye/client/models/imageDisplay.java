package app.insightfuleye.client.models;

public class imageDisplay {

    public imageDisplay(String imagePath, int position) {
        this.imagePath = imagePath;
        this.position = position;
    }

    private String imagePath;
    private int position;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
