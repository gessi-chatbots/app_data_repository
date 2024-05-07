package upc.edu.gessi.repo.repository;

import upc.edu.gessi.repo.exception.MobileApplicationNotFoundException;

import java.io.Serializable;
import java.util.List;

public interface RDFCRUDRepository<T extends Serializable> {
    List<T> findAll() throws Exception;
    List<T> findAllPaginated(final Integer page, final Integer size) throws MobileApplicationNotFoundException;

    T insert(T entity);

    T update(T entity);

    void delete(String id);
}
