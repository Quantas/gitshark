package com.quantasnet.gitserver.git.backend.mongo;

import java.io.IOException;

import org.eclipse.jgit.internal.storage.dfs.DfsObjDatabase;
import org.eclipse.jgit.internal.storage.dfs.DfsRefDatabase;
import org.eclipse.jgit.internal.storage.dfs.DfsRepository;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryBuilder;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;

import com.quantasnet.gitserver.backend.mongo.MongoService;

/**
 * 
 * @author Andrew
 */
public class MongoDfsRepository extends DfsRepository {

	private static class MongoRepositoryBuilder extends DfsRepositoryBuilder<MongoRepositoryBuilder, MongoDfsRepository> {
		@Override
		public MongoDfsRepository build() throws IOException {
			return new MongoDfsRepository(this);
		}
	}

	private String id;
	private MongoService mongoService;
	
	public MongoDfsRepository(final String id, final String repoName, final MongoService mongoService) {
		this(new MongoRepositoryBuilder().setRepositoryDescription(new DfsRepositoryDescription(repoName)));
		this.id = id;
		this.mongoService = mongoService;
	}
	
	private MongoDfsRepository(final MongoRepositoryBuilder builder) {
		super(builder);
	}

	@Override
	public DfsObjDatabase getObjectDatabase() {
		return new MongoDfsObjDatabase(this, mongoService);
	}

	@Override
	public DfsRefDatabase getRefDatabase() {
		return new MongoDfsRefDatabase(this, mongoService);
	}

	public String getId() {
		return id;
	}
}
