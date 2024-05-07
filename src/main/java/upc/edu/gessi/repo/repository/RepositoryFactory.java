package upc.edu.gessi.repo.repository;



public interface RepositoryFactory {
    Object createRepository(Class<?> clazz);
}
