package upc.edu.gessi.repo.controller;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/api/v1")
public interface BaseAPI {
    @PostMapping("/ping")
    @ResponseStatus(HttpStatus.OK)
    void ping();
}
