package com.quantasnet.gitshark.git.dfs.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by andrewlandsverk on 8/23/15.
 */
public interface MongoRepoSecurityRepository extends MongoRepository<MongoRepoSecurity, String> {
	MongoRepoSecurity findByRepoId(String repoId);
}