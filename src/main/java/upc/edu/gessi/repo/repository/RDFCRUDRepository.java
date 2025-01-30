package upc.edu.gessi.repo.repository;

import org.eclipse.rdf4j.model.IRI;
import upc.edu.gessi.repo.exception.NoObjectFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.exception.Reviews.NoReviewsFoundException;

import java.io.Serializable;
import java.util.List;

public interface RDFCRUDRepository<T extends Serializable> {
    T findById(String id) throws ObjectNotFoundException, NoReviewsFoundException;
    List<T> findAll() throws NoObjectFoundException;
    List<T> findAllPaginated(final Integer page, final Integer size) throws NoObjectFoundException;

    IRI insert(T dto);

    T update(T entity);

    void delete(String id);
}
