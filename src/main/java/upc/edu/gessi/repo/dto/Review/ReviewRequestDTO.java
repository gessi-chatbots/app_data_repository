
package upc.edu.gessi.repo.dto.Review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO implements Serializable {
    private String reviewId;
}
