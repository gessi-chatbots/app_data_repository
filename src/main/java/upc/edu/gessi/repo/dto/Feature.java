package upc.edu.gessi.repo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Feature implements Serializable {

    @JsonProperty("package")
    private String package_name;
    private String name;

    public Feature(String package_name, String name) {
        this.package_name = package_name;
        this.name = name;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
