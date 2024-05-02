package upc.edu.gessi.repo.controller;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface CrudAPI<T> extends BaseAPI {
    @PostMapping("/ping")
    @ResponseStatus(HttpStatus.OK)
    void ping();

    @GetMapping("/")
    T get();

    @GetMapping("/all")
    List<T> getAll();

    @PostMapping("/")
    T create(@RequestBody T entity);

    @PutMapping("/")
    T update(@RequestBody T entity);

    @DeleteMapping("/")
    void delete();
}
