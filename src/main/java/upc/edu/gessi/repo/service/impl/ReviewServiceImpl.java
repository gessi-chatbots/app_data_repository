package upc.edu.gessi.repo.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.Review.*;
import upc.edu.gessi.repo.exception.NoObjectFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.exception.Reviews.NoReviewsFoundException;
import upc.edu.gessi.repo.repository.RepositoryFactory;
import upc.edu.gessi.repo.repository.ReviewRepository;
import upc.edu.gessi.repo.repository.SentenceRepository;
import upc.edu.gessi.repo.service.ReviewService;

import java.util.ArrayList;
import java.util.List;


@Service
@Lazy
public class ReviewServiceImpl implements ReviewService {
    private final RepositoryFactory repositoryFactory;
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);


    @Autowired
    public ReviewServiceImpl(final RepositoryFactory repoFact) {
        repositoryFactory = repoFact;
    }

    @Override
    public List<ReviewDTO> create(List<ReviewDTO> dtos) {
        for (ReviewDTO r : dtos) {
            try {
                insertReview(r);
                logger.info("Inserted review {} ", r.getId());
                insertReviewSentences(r);
                logger.info("Inserted review {} sentences", r.getId());
            } catch (Exception e) {
                logger.error("Failed to save in Graph: " + r.toString());
            }

        }
        return dtos;
    }

    private void insertReviewSentences(ReviewDTO r) {
        for(SentenceDTO sentenceDTO : r.getSentences()) {
            ((SentenceRepository) useRepository(SentenceRepository.class)).insert(sentenceDTO);
            ((ReviewRepository) useRepository(ReviewRepository.class))
                    .addSentenceToReview(
                            r.getId(),
                            sentenceDTO.getId()
                    );
        }
    }

    private void insertReview(ReviewDTO r) {
        ((ReviewRepository) useRepository(ReviewRepository.class)).insert(r);
    }

    @Override
    public ReviewDTO get(String id) throws ObjectNotFoundException, NoReviewsFoundException {
        return ((ReviewRepository) useRepository(ReviewRepository.class)).findById(id);
    }

    @Override
    public List<ReviewDTO> getListed(List<String> ids) throws NoObjectFoundException {
        return ((ReviewRepository) useRepository(ReviewRepository.class)).findListed(ids);
    }

    @Override
    public List<ReviewDTO> getAllPaginated(Integer page, Integer size) throws NoObjectFoundException {
        return ((ReviewRepository) useRepository(ReviewRepository.class)).findAllPaginated(page, size);
    }

    @Override
    public List<ReviewDTO> getAll() throws NoObjectFoundException {
        return ((ReviewRepository) useRepository(ReviewRepository.class)).findAll();
    }

    @Override
    public void update(ReviewDTO entity) {
        ((ReviewRepository) useRepository(ReviewRepository.class)).update(entity);
    }

    @Override
    public void delete(String id) {
        ((ReviewRepository) useRepository(ReviewRepository.class)).delete(id);
    }

    private Object useRepository(Class<?> clazz) {
        return repositoryFactory.createRepository(clazz);
    }

    @Override
    public List<ReviewDTO> getBatched(int batch, int offset) {
        return ((ReviewRepository) useRepository(ReviewRepository.class)).findBatched(batch, offset);
    }

    @Override
    public List<ReviewDTO> getAllSimplified() {
        return ((ReviewRepository) useRepository(ReviewRepository.class)).findAllSimplified();
    }
    @Override
    public Integer getReviewCount() {
        return ((ReviewRepository) useRepository(ReviewRepository.class)).getCount();
    }

    @Override
    public List<ReviewDTO> getByFeature(String feature) {
        // TODO too much overhead due to graphDB, use the getByFeatures best
        return new ArrayList<>();
    }

    @Override
    public List<ReviewDescriptorResponseDTO> getByDescriptors(ReviewDescriptorRequestDTO requestDTO,
                                                              int page,
                                                              int size) throws NoReviewsFoundException {


        return ((ReviewRepository) useRepository(ReviewRepository.class)).findByDescriptors(
                        requestDTO,
                        page,
                        size
                );

    }

    @Override
    public Long getReviewCountByDescriptors(ReviewDescriptorRequestDTO requestDTO) throws NoReviewsFoundException {
        return ((ReviewRepository) useRepository(ReviewRepository.class)).countByDescriptors(
                requestDTO
        );
    }

}
