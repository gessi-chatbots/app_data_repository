package upc.edu.gessi.repo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
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
public class ApplicationDTO implements Serializable {
    private String name;
    private String packageName;
    private String description;
    private String summary;
    private String category;
    private String categoryId;
    private String version;
    private String androidVersion;
    private String genre;
    private String changelog;
    private String developer;
    private String developerSite;
    private List<Review> reviews;
    @Deprecated
    private List<AppCategory> categories;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy")
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date releaseDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy")
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date currentVersionReleaseDate;
    private List<String> tags;
    private List<Feature> features;


}
