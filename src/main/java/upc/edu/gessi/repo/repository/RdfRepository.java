package upc.edu.gessi.repo.repository;

import upc.edu.gessi.repo.exception.ApplicationNotFoundException;

import java.io.Serializable;
import java.util.List;

public interface RdfRepository <T extends Serializable> {
    List<T> findAll() throws Exception;

    List<T> findAllSimplified() throws ApplicationNotFoundException;

    List<T> findAllSimplifiedPaginated(final Integer page, final Integer size) throws ApplicationNotFoundException;
}