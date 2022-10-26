package upc.edu.gessi.repo.domain;

import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class App implements Serializable {

    private String app_name;
    private String package_name;
    private String description;
    private String summary;
    private String category;
    private String version;
    private String android_version;
    private String genre;
    private String changelog;
    private String developer;
    private String developer_site;
    private List<Review> reviews;
    private List<AppCategory> categories;

    public List<String> getFeatures() {
        //otherwise it leads to problems when inserting the app
        if (features != null)
            return features;
        else return new ArrayList<>();
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    private List<String> features;
    private List<String> tags;


    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public String getApp_name() {
        return this.app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAndroid_version() {
        return android_version;
    }

    public void setAndroid_version(String android_version) {
        this.android_version = android_version;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getDeveloper_site() {
        return developer_site;
    }

    public void setDeveloper_site(String developer_site) {
        this.developer_site = developer_site;
    }

    public List<AppCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<AppCategory> categories) {
        this.categories = categories;
    }
}

