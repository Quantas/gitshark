package com.quantasnet.gitserver.git.dfs;

import java.io.IOException;

import org.eclipse.jgit.internal.storage.dfs.DfsObjDatabase;
import org.eclipse.jgit.internal.storage.dfs.DfsRefDatabase;
import org.eclipse.jgit.internal.storage.dfs.DfsRepository;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryBuilder;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;

import com.quantasnet.gitserver.git.dfs.mongo.MongoService;

/**
 * 
 * @author Andrew
 */
public class GitSharkDfsRepository extends DfsRepository {

	private static class MongoRepositoryBuilder extends DfsRepositoryBuilder<MongoRepositoryBuilder, GitSharkDfsRepository> {
		@Override
		public GitSharkDfsRepository build() throws IOException {
			return new GitSharkDfsRepository(this);
		}
	}

	private String id;
	private MongoService mongoService;
	
	public GitSharkDfsRepository(final String id, final String repoName, final MongoService mongoService) {
		this(new MongoRepositoryBuilder().setRepositoryDescription(new DfsRepositoryDescription(repoName)));
		this.id = id;
		this.mongoService = mongoService;
	}
	
	private GitSharkDfsRepository(final MongoRepositoryBuilder builder) {
		super(builder);
	}

	@Override
	public DfsObjDatabase getObjectDatabase() {
		return new GitSharkDfsObjDatabase(this, mongoService);
	}

	@Override
	public DfsRefDatabase getRefDatabase() {
		return new GitSharkDfsRefDatabase(this, mongoService);
	}

	public String getId() {
		return id;
	}
}
