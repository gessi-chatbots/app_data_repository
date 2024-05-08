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
    private final ReviewRepositoryImpl reviewRepositoryImpl;

    @Autowired
    public RepositoryFactoryImpl(final ReviewRepositoryImpl reviewRepositoryImpl,
                                 final MobileApplicationRepository mobileApplicationRepository) {
        this.reviewRepositoryImpl = reviewRepositoryImpl;
        this.mobileApplicationRepository = mobileApplicationRepository;
    }

    @Override
    public Object createRepository(final Class<?> clazz) {
        if (clazz == ReviewRepositoryImpl.class) {
            return reviewRepositoryImpl;
        } else if (clazz == MobileApplicationRepository.class) {
            return mobileApplicationRepository;
        }
        throw new IllegalArgumentException("Not valid class: " + clazz.getName());
    }
}
