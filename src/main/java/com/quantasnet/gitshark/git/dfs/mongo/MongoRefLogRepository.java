package com.quantasnet.gitshark.git.dfs.mongo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoRefLogRepository extends MongoRepository<MongoRefLog, String> {
	List<MongoRefLog> findByRepoId(String repoId);
}
