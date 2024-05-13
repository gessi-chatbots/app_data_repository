package upc.edu.gessi.repo.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import upc.edu.gessi.repo.dto.Review.*;
import upc.edu.gessi.repo.exception.NoObjectFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.repository.RepositoryFactory;
import upc.edu.gessi.repo.repository.ReviewRepository;
import upc.edu.gessi.repo.repository.impl.ReviewRepositoryImpl;
import upc.edu.gessi.repo.service.ReviewService;

import java.util.List;


@Service
@Lazy
public class ReviewServiceImpl implements ReviewService {
    private final RepositoryFactory repositoryFactory;


    @Autowired
    public ReviewServiceImpl(final RepositoryFactory repoFact) {
        repositoryFactory = repoFact;
    }

    @Override
    public List<ReviewDTO> create(List<ReviewDTO> dtos) {
        for (ReviewDTO r : dtos) {
            // Todo Extract Sentences and use Sentence Repository
            ((ReviewRepository) useRepository(ReviewRepository.class)).insert(r);
        }
        return dtos;
    }

    @Override
    public ReviewDTO get(String id) throws ObjectNotFoundException {
        return ((ReviewRepositoryImpl) useRepository(ReviewRepository.class)).findById(id);
    }

    @Override
    public List<ReviewDTO> getListed(List<String> ids) throws NoObjectFoundException {
        return ((ReviewRepositoryImpl) useRepository(ReviewRepository.class)).findListed(ids);
    }

    @Override
    public List<ReviewDTO> getAllPaginated(Integer page, Integer size) throws NoObjectFoundException {
        return ((ReviewRepositoryImpl) useRepository(ReviewRepository.class)).findAllPaginated(page, size);
    }

    @Override
    public List<ReviewDTO> getAll() throws NoObjectFoundException {
        return ((ReviewRepositoryImpl) useRepository(ReviewRepository.class)).findAll();
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

}
