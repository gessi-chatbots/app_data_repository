package upc.edu.gessi.repo.service;

import upc.edu.gessi.repo.exception.NoObjectFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;
import upc.edu.gessi.repo.exception.Reviews.NoReviewsFoundException;

import java.util.List;

public interface CrudService<T> {
    List<T> create(List<T> dtos);

    T get(String id) throws ObjectNotFoundException, NoReviewsFoundException;

    List<T> getListed(List<String> ids) throws NoObjectFoundException;

    List<T> getAllPaginated(Integer page,
                            Integer size)
            throws NoObjectFoundException;


    List<T> getAll() throws NoObjectFoundException;

    void update(T entity);

    void delete(String id);
}
