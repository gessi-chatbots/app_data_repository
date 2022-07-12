package upc.edu.gessi.repo;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class App implements Serializable {
    private String name;
    private String description;
    private String softwareVersion;
    private String genre;
    private String releaseNotes;

    public List<Map<String, String>> getReviews() {
        return reviews;
    }

    public void setReviews(List<Map<String, String>> reviews) {
        this.reviews = reviews;
    }

    private List<Map<String,String>> reviews;


    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}

