package upc.edu.gessi.repo.service;

import upc.edu.gessi.repo.exception.ObjectNotFoundException;

import java.util.List;

public interface CrudService<T> {
    List<T> create(List<T> dtos);

    T get(String id) throws ObjectNotFoundException;

    List<T> getListed(List<String> id) throws ObjectNotFoundException;

    List<T> getAllPaginated(Integer page,
                            Integer size)
            throws ObjectNotFoundException, ClassNotFoundException, IllegalAccessException;


    List<T> getAll();

    void update(T entity);

    void delete(String id);
}
