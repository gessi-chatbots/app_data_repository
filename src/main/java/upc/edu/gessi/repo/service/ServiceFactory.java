package upc.edu.gessi.repo.service;

public interface ServiceFactory {
    Object createService(Class<?> clazz);
}
