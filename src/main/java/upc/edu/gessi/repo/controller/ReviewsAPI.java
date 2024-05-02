package upc.edu.gessi.repo.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public interface ReviewsAPI extends CrudAPI<ReviewDTO> {

    @PostMapping(value = "/reviews", produces = "application/json")
    @ApiOperation(value = "Insert Data (JSON format)", notes = "Inserts a list of review entities into the GraphDB. The " +
            "data is sent in JSON format through the request body.")
    ResponseEntity<String> insertJSONReviewData(@RequestBody List<ReviewDTO> completeApplicationDataDTOS);

}
