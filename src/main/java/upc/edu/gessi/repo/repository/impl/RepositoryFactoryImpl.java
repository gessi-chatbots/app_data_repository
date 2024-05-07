package upc.edu.gessi.repo.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import upc.edu.gessi.repo.repository.MobileApplicationRepository;
import upc.edu.gessi.repo.repository.RepositoryFactory;

@Component
@Lazy
public class RepositoryFactoryImpl implements RepositoryFactory {

    private final MobileApplicationRepository mobileApplicationRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    public RepositoryFactoryImpl(final ReviewRepository reviewRepository,
                                 final MobileApplicationRepository mobileApplicationRepository) {
        this.reviewRepository = reviewRepository;
        this.mobileApplicationRepository = mobileApplicationRepository;
    }

    @Override
    public Object createRepository(final Class<?> clazz) {
        if (clazz == ReviewRepository.class) {
            return reviewRepository;
        } else if (clazz == MobileApplicationRepository.class) {
            return mobileApplicationRepository;
        }
        throw new IllegalArgumentException("Not valid class: " + clazz.getName());
    }
}
