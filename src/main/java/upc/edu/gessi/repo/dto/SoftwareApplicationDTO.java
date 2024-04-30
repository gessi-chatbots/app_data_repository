package upc.edu.gessi.repo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.serializer.CustomDateDeserializer;
import upc.edu.gessi.repo.dto.serializer.CustomDateSerializer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SoftwareApplicationDTO implements Serializable {
    @JsonProperty("app_name")
    private String name;
    private String description;
    private String summary;
    private String category;
    private String categoryId;
    @JsonProperty("in_app_purchases")
    private Boolean inAppPurchases;
    private String genre;
    @JsonProperty("android_version")
    private String androidVersion;
    private String developer;
    @JsonProperty("developer_site")
    private String developerSite;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy")
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonProperty("release_date")
    private Date releaseDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy")
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonProperty("current_version_release_date")
    private Date currentVersionReleaseDate;
    private String version;
    private String changelog;
    @JsonProperty("reviews")
    private List<ReviewDTO> reviewDTOS;
    @JsonProperty("package_name")
    private String packageName;
    private List<String> features;
    private List<String> tags;
    private List<String> categories;
}
