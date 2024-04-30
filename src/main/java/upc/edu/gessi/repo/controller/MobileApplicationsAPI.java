package upc.edu.gessi.repo.controller;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("/graph-db-api")

public interface MobileApplicationsAPI<T>  {
    @PostMapping("/ping")
    @ResponseStatus(HttpStatus.OK)
    void ping();

}
