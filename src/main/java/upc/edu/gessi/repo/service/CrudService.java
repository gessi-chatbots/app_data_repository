package upc.edu.gessi.repo.service;

import upc.edu.gessi.repo.exception.ObjectNotFoundException;

import java.util.List;

public interface CrudService<T> {
    List<T> create(List<T> entity);

    T get(String id) throws ObjectNotFoundException;

    List<T> getListed(List<String> id) throws ObjectNotFoundException;

    List<T> getAllPaginated(boolean paginated,
                            Integer page,
                            Integer size)
            throws ObjectNotFoundException, ClassNotFoundException, IllegalAccessException;


    List<T> getAll();

    T update(T entity);

    Void delete();
}