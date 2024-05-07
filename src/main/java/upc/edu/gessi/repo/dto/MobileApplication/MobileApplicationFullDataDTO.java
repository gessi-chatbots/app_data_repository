package upc.edu.gessi.repo.dto.MobileApplication;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.serializer.CustomDateDeserializer;
import upc.edu.gessi.repo.dto.serializer.CustomDateSerializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MobileApplicationFullDataDTO extends MobileApplicationDTO implements Serializable {

    @JsonProperty("description")
    private String description;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("developer")
    private String developer;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy")
    @JsonProperty("release_date")
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date releaseDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy")
    @JsonProperty("current_version_release_date")
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date currentVersionReleaseDate;

    @JsonProperty("version")
    private String version;

    @JsonProperty("changelog")
    private String changelog;

    @JsonProperty("reviews")
    private List<ReviewDTO> reviews = new ArrayList<>();

    @JsonProperty("review_count")
    private Integer reviewCount;

    @JsonProperty("android_version")
    private String androidVersion;

    @JsonProperty("developer_site")
    private String developerSite;

    @JsonProperty("in_app_purchases")
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