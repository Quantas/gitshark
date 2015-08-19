package com.quantasnet.gitserver.backend.mongo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
interface MongoRepoRepository extends MongoRepository<MongoRepo, String> {

	List<MongoRepo> findByOwnerId(long ownerId);
	MongoRepo findByOwnerIdAndName(long ownerId, String name);
	
}
