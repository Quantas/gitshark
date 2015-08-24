package com.quantasnet.gitshark.git.dfs.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

interface MongoRepoSecurityRepository extends MongoRepository<MongoRepoSecurity, String> {
	MongoRepoSecurity findByRepoId(String repoId);
}