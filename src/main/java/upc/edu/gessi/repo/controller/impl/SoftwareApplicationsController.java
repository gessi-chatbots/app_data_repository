package upc.edu.gessi.repo.controller.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import upc.edu.gessi.repo.controller.SoftwareApplicationsAPI;
import upc.edu.gessi.repo.dto.*;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;

import java.util.List;

@RestController
public class SoftwareApplicationsController implements SoftwareApplicationsAPI {

    private final Logger logger = LoggerFactory.getLogger(SoftwareApplicationsController.class);

    @Autowired
    public SoftwareApplicationsController() {}


    @Override
    public void ping() {}


    @Override
    public ResponseEntity<SoftwareApplicationDTO> create(List entity) {
        return null;
    }

    @Override
    public ResponseEntity<SoftwareApplicationDTO> get(String id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public ResponseEntity<List<SoftwareApplicationDTO>> getListed(List<String> id) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public ResponseEntity<List<SoftwareApplicationDTO>> getAllPaginated(boolean paginated, Integer page, Integer size, boolean simplified)
            throws ObjectNotFoundException, ClassNotFoundException, IllegalAccessException {
        return null;
    }

    @Override
    public ResponseEntity<List<SoftwareApplicationDTO>> getAll() {
        return null;
    }

    @Override
    public ResponseEntity<SoftwareApplicationDTO> update(SoftwareApplicationDTO entity) {
        return null;
    }

    @Override
    public ResponseEntity<Void> delete() {
        return null;
    }

}
