package upc.edu.gessi.repo.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import upc.edu.gessi.repo.repository.RdfFactory;

@Component
@Lazy
public class RdfFactoryImpl implements RdfFactory {

    private final MobileApplicationRepository mobileApplicationRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    public RdfFactoryImpl(final ReviewRepository reviewRepository,
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
