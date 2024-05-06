package upc.edu.gessi.repo.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.controller.DigitalDocumentsAPI;
import upc.edu.gessi.repo.dto.DigitalDocumentDTO;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;

import java.util.List;

@RestController
public class DigitalDocumentsController implements DigitalDocumentsAPI {


    @Autowired
    public DigitalDocumentsController() {
    }

    @Override
    public void ping() {}


    @Override
    public ResponseEntity<List<DigitalDocumentDTO>> create(final List<DigitalDocumentDTO> entities) {
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<String> insertDigitalDocumentsJSONData(final List<DigitalDocumentDTO> digitalDocumentDTOS) {
        return new ResponseEntity<>("Digital documents inserted successfully", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<DigitalDocumentDTO> get(final String id) throws ObjectNotFoundException {
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<List<DigitalDocumentDTO>> getListed(final List<String> ids) throws ObjectNotFoundException {
        return null;
    }

    @Override
    public ResponseEntity<List<DigitalDocumentDTO>> getAllPaginated(final Integer page,
                                                                    final Integer size)
            throws ObjectNotFoundException, ClassNotFoundException, IllegalAccessException {
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<List<DigitalDocumentDTO>> getAll() {
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<DigitalDocumentDTO> update(final DigitalDocumentDTO entity) {
        return ResponseEntity.ok(null);
    }

    @Override
    public ResponseEntity<Void> delete(String id) {
        return ResponseEntity.noContent().build();
    }
}