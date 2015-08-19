package com.quantasnet.gitserver.security.rememberme;

import org.springframework.data.mongodb.repository.MongoRepository;

interface RememberMeTokenRepo extends MongoRepository<RememberMeToken, String> {
    void deleteRememberMeTokenByUsername(String username);
}
