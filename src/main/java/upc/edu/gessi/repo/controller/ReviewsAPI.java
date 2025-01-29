package upc.edu.gessi.repo.controller;


import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.dto.PageResponseDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDescriptorRequestDTO;
import upc.edu.gessi.repo.dto.Review.ReviewDescriptorResponseDTO;

import java.util.List;


@RestController
@RequestMapping("/reviews")
public interface ReviewsAPI extends CrudAPI<ReviewDTO> {
    @ApiOperation("Generates a .ttl file")
    @GetMapping(value = "/extract")
    ResponseEntity<byte[]> extractReviews(
            @RequestParam(name = "size", defaultValue = "10000", required = false) Integer size,
            @RequestParam(name = "market-segment", defaultValue = "Communication", required = false) String marketSegment);

    @ApiOperation("Fetch reviews based on filters (descriptors)")
    @PostMapping(value = "/by-descriptors")
    public ResponseEntity<PageResponseDTO<ReviewDescriptorResponseDTO>> getReviewsByDescriptors(
            @RequestBody ReviewDescriptorRequestDTO request,
            @RequestParam(value = "page", defaultValue = "0", required = false) final Integer page,
            @RequestParam(value = "size", defaultValue = "10", required = false) final Integer size);
}
