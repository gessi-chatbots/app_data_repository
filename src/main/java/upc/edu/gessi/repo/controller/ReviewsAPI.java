package upc.edu.gessi.repo.controller;


import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.Review.ReviewFeatureRequestDTO;
import upc.edu.gessi.repo.dto.Review.ReviewFeatureResponseDTO;

import java.util.List;


@RestController
@RequestMapping("/reviews")
public interface ReviewsAPI extends CrudAPI<ReviewDTO> {
    @ApiOperation("Generates a .ttl file")
    @GetMapping(value = "/extract")
    ResponseEntity<byte[]> extractReviews(
            @RequestParam(name = "size", defaultValue = "10000", required = false) Integer size,
            @RequestParam(name = "market-segment", defaultValue = "Communication", required = false) String marketSegment);

    @ApiOperation("Fetch reviews based on features")
    @PostMapping(value = "/by-features")
    ResponseEntity<List<ReviewFeatureResponseDTO>> getReviewsByFeatures(
            @RequestBody ReviewFeatureRequestDTO request);
}
