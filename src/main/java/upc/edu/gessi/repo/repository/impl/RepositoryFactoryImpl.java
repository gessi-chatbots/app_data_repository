package upc.edu.gessi.repo.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import upc.edu.gessi.repo.repository.RepositoryFactory;

@Component
@Lazy
public class RepositoryFactoryImpl implements RepositoryFactory {

    private final MobileApplicationRepositoryImpl mobileApplicationRepositoryImpl;
    private final ReviewRepository reviewRepository;

    @Autowired
    public RepositoryFactoryImpl(final ReviewRepository reviewRepository,
                                 final MobileApplicationRepositoryImpl mobileApplicationRepositoryImpl) {
        this.reviewRepository = reviewRepository;
        this.mobileApplicationRepositoryImpl = mobileApplicationRepositoryImpl;
    }

    @Override
    public Object createRepository(final Class<?> clazz) {
        if (clazz == ReviewRepository.class) {
            return reviewRepository;
        } else if (clazz == MobileApplicationRepositoryImpl.class) {
            return mobileApplicationRepositoryImpl;
        }
        throw new IllegalArgumentException("Not valid class: " + clazz.getName());
    }
}
