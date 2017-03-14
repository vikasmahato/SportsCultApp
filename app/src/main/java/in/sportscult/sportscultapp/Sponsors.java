package in.sportscult.sportscultapp;

/**
 * Created by Vikas on 14-03-2017.
 */

public class Sponsors {
    private String name;
    private int thumbnail;

    public Sponsors() {
    }

    public Sponsors(String name, int thumbnail) {
        this.name = name;
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }
}
