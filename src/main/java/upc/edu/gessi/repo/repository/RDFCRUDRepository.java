package upc.edu.gessi.repo.repository;

import upc.edu.gessi.repo.exception.MobileApplications.MobileApplicationNotFoundException;
import upc.edu.gessi.repo.exception.NoObjectFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.exception.Reviews.ReviewNotFoundException;

import java.io.Serializable;
import java.util.List;

public interface RDFCRUDRepository<T extends Serializable> {
    T findById(String id) throws ObjectNotFoundException;
    List<T> findAll() throws NoObjectFoundException;
    List<T> findAllPaginated(final Integer page, final Integer size) throws NoObjectFoundException;

    T insert(T entity);

    T update(T entity);

    void delete(String id);
}
