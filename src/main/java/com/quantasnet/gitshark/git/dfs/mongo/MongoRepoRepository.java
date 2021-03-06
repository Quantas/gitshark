package com.quantasnet.gitshark.git.dfs.mongo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

interface MongoRepoRepository extends MongoRepository<MongoRepo, String> {
	List<MongoRepo> findByOwnerId(String ownerId);
	MongoRepo findByOwnerIdAndName(String ownerId, String name);
}
