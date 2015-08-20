package com.quantasnet.gitshark.user;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    User getUserByUserName(String userName);
    User getUserByEmail(String email);
}
