package upc.edu.gessi.repo.dto.Review;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import upc.edu.gessi.repo.dto.LanguageModel.LanguageModelDTO;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolarityDTO implements Serializable {
    private String polarity;
    private LanguageModelDTO languageModel;
}
