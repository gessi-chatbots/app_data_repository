package upc.edu.gessi.repo.repository;

import java.util.List;

public interface RdfRepository <T> {
    List<T> findAll() throws Exception;
}
