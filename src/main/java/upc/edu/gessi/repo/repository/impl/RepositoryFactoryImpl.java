package upc.edu.gessi.repo.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import upc.edu.gessi.repo.repository.*;

@Component
@Lazy
public class RepositoryFactoryImpl implements RepositoryFactory {

    private final MobileApplicationRepository mobileApplicationRepository;
    private final ReviewRepository reviewRepository;
    private final SentenceRepository sentenceRepository;
    private final FeatureRepository featureRepository;

    @Autowired
    public RepositoryFactoryImpl(final ReviewRepository reviewRepository,
                                 final MobileApplicationRepository mobileApplicationRepository,
                                 final SentenceRepository sentenceRepo,
                                 final FeatureRepository featureRepo) {
        this.reviewRepository = reviewRepository;
        this.mobileApplicationRepository = mobileApplicationRepository;
        this.sentenceRepository = sentenceRepo;
        this.featureRepository = featureRepo;
    }

    @Override
    public Object createRepository(final Class<?> clazz) {
        if (clazz == ReviewRepository.class) {
            return reviewRepository;
        } else if (clazz == MobileApplicationRepository.class) {
            return mobileApplicationRepository;
        }  else if (clazz == SentenceRepository.class) {
            return sentenceRepository;
        } else if (clazz == FeatureRepository.class) {
            return featureRepository;
        }
        throw new IllegalArgumentException("Not valid class: " + clazz.getName());
    }
}
