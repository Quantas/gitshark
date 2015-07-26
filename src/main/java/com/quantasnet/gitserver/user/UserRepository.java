package com.quantasnet.gitserver.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User getUserByUserName(String userName);
    User getUserByEmail(String email);
}
