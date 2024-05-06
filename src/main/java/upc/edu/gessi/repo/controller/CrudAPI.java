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

    @GetMapping("/list")
    ResponseEntity<List<T>> getListed(@RequestBody List<String> ids) throws ObjectNotFoundException;

    @GetMapping(value = "/", params = {"paginated"}, produces = "application/json")
    @ResponseBody
    ResponseEntity<List<T>> getAllPaginated(
            @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(value = "size", defaultValue = "20", required = false) Integer size)
            throws ObjectNotFoundException, ClassNotFoundException, IllegalAccessException;

    @GetMapping("/paginated")
    ResponseEntity<List<T>> getAll();

    @PutMapping("/")
    ResponseEntity<T> update(@RequestBody T entity);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable String id);
}
