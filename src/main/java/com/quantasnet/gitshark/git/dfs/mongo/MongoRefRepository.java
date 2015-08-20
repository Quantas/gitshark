package com.quantasnet.gitshark.git.dfs.mongo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

interface MongoRefRepository extends MongoRepository<MongoRef, String> {
	List<MongoRef> findByRepoId(String repoId);
	MongoRef findByRepoIdAndName(String repoId, String name);
}