package upc.edu.gessi.repo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileApplicationDTO implements Serializable {

    @JsonProperty("name")
    private String appName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("author")
    private String developer;

    @JsonProperty("package")
    private String packageName;

    @JsonProperty("releaseDate")
    private Date releaseDate;

    @JsonProperty("currentVersionReleaseDate")
    private Date currentVersionReleaseDate;

    @JsonProperty("version")
    private String version;

    @JsonProperty("changelog")
    private String changelog;

    @JsonProperty("reviews")
    private List<ReviewDTO> reviewDTOS = new ArrayList<>();

    @JsonProperty("androidVersion")
    private String androidVersion;

    @JsonProperty("developerSite")
    private String developerSite;

    @JsonProperty("inAppPurchases")
    private Boolean inAppPurchases;

    @JsonProperty("genre")
    private String genre;

    @JsonProperty("categories")
    private List<String> categories = new ArrayList<>();

    @JsonProperty("categoryId")
    private String categoryId;

    @JsonProperty("category")
    private String category;

    @JsonProperty("features")
    private List<String> features = new ArrayList<>();

    @JsonProperty("tags")
    private List<String> tags = new ArrayList<>();
}
