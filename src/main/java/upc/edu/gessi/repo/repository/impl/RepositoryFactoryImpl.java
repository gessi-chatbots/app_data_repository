package upc.edu.gessi.repo.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import upc.edu.gessi.repo.repository.MobileApplicationRepository;
import upc.edu.gessi.repo.repository.RepositoryFactory;
import upc.edu.gessi.repo.repository.ReviewRepository;
import upc.edu.gessi.repo.repository.SentenceRepository;

@Component
@Lazy
public class RepositoryFactoryImpl implements RepositoryFactory {

    private final MobileApplicationRepository mobileApplicationRepository;
    private final ReviewRepository reviewRepository;
    private final SentenceRepository sentenceRepository;

    @Autowired
    public RepositoryFactoryImpl(final ReviewRepository reviewRepository,
                                 final MobileApplicationRepository mobileApplicationRepository,
                                 final SentenceRepository sentenceRepo) {
        this.reviewRepository = reviewRepository;
        this.mobileApplicationRepository = mobileApplicationRepository;
        this.sentenceRepository = sentenceRepo;
    }

    @Override
    public Object createRepository(final Class<?> clazz) {
        if (clazz == ReviewRepository.class) {
            return reviewRepository;
        } else if (clazz == MobileApplicationRepository.class) {
            return mobileApplicationRepository;
        }  else if (clazz == SentenceRepository.class) {
            return sentenceRepository;
        }
        throw new IllegalArgumentException("Not valid class: " + clazz.getName());
    }
}
