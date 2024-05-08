package upc.edu.gessi.repo.dto.Review;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import upc.edu.gessi.repo.dto.serializer.CustomDateDeserializer;
import upc.edu.gessi.repo.dto.serializer.CustomDateSerializer;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewDTO implements Serializable {

    @JsonProperty("package_name")
    private String packageName;

    @JsonProperty("reviewId")
    private String id;

    @JsonProperty("review")
    private String reviewText;

    @JsonProperty("userName")
    private String author;

    @JsonProperty("applicationId")
    private String applicationId;

    @JsonProperty("score")
    private Integer rating;

    private String source;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MMM dd, yyyy")
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonProperty("date")
    private Date date;

    private List<SentenceDTO> sentences;
}
