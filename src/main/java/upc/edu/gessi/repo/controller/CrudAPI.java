package upc.edu.gessi.repo.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.edu.gessi.repo.exception.ObjectNotFoundException;

import java.util.List;

public interface CrudAPI<T> extends BaseAPI {

    @PostMapping("/")
    ResponseEntity<List<T>> create(@RequestBody List<T> entity);

    @GetMapping("/{id}")
    ResponseEntity<T> get(@PathVariable String id) throws ObjectNotFoundException;

    @GetMapping("/")
    ResponseEntity<List<T>> getListed(@PathVariable List<String> id) throws ObjectNotFoundException;

    @GetMapping(value = "/all", params = {"paginated"}, produces = "application/json")
    @ResponseBody
    ResponseEntity<List<T>> getAllPaginated(
            @RequestParam(value = "paginated", defaultValue = "false", required = false) boolean paginated,
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "20", required = false) Integer size)
            throws ObjectNotFoundException, ClassNotFoundException, IllegalAccessException;

    @GetMapping("/all")
    ResponseEntity<List<T>> getAll();

    @PutMapping("/")
    ResponseEntity<T> update(@RequestBody T entity);

    @DeleteMapping("/")
    ResponseEntity<Void> delete();
}
