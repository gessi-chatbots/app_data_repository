package upc.edu.gessi.repo.controller.impl;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import upc.edu.gessi.repo.controller.DocumentsAPI;
import upc.edu.gessi.repo.exception.NoObjectFoundException;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;

import java.util.List;

@RestController
public class DocumentsController implements DocumentsAPI {

    @Override
    public void ping() {

    }

    @Override
    public ResponseEntity<List<String>> create(List<String> entity) {
        return null;
    }

    @Override
    public ResponseEntity<String> get(String id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public ResponseEntity<List<String>> getListed(List<String> ids) throws NoObjectFoundException {
        return null;
    }

    @Override
    public ResponseEntity<List<String>> getAllPaginated(final Integer page, final Integer size) throws NoObjectFoundException {
        return null;
    }

    @Override
    public ResponseEntity<List<String>> getAll() {
        return null;
    }

    @Override
    public ResponseEntity<String> update(String entity) {
        return null;
    }

    @Override
    public ResponseEntity<Void> delete(String id) {
        return null;
    }
}