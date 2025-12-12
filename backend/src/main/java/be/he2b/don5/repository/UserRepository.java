package be.he2b.don5.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import be.he2b.don5.model.User;

public interface UserRepository extends MongoRepository<User, String>{
    Optional<User> findByEmail(String email);
    
}
