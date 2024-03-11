package upc.edu.gessi.repo.repository;

import java.io.Serializable;
import java.util.List;

public interface RdfRepository <T extends Serializable> {
    List<T> findAll() throws Exception;

}
