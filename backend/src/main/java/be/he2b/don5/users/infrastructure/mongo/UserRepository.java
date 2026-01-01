package be.he2b.don5.users.infrastructure.mongo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import be.he2b.don5.users.domain.User;

public interface UserRepository extends MongoRepository<User, String>{
    Optional<User> findByEmail(String email);
    
}
