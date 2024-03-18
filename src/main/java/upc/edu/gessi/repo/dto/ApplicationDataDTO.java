package upc.edu.gessi.repo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import upc.edu.gessi.repo.dto.Review.ReviewRequestDTO;
import upc.edu.gessi.repo.dto.Review.ReviewResponseDTO;

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
public class ApplicationDataDTO implements Serializable {
    private String name;
    private String author;
    private String applicationPackage;
    private Date releaseDate;
    private String version;
    private List<String> categories = new ArrayList<>();
    private List<ReviewResponseDTO> reviews = new ArrayList<>();
}