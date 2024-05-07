package upc.edu.gessi.repo.controller;


import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;


@RestController
@RequestMapping("/reviews")
public interface ReviewsAPI extends CrudAPI<ReviewDTO> {
}
