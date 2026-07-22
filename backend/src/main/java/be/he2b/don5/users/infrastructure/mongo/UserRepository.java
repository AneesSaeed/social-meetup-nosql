package be.he2b.don5.users.infrastructure.mongo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import be.he2b.don5.users.domain.User;

/**
 * MongoDB repository for {@link User}.
 *
 * <p>Provides basic CRUD operations (save, find, delete, etc.) through
 * {@link MongoRepository} and adds custom queries needed by the module.
 */
public interface UserRepository extends MongoRepository<User, String>{
    Optional<User> findByEmail(String email);
}
