package com.quantasnet.gitshark.git.dfs;

import java.io.IOException;

import org.eclipse.jgit.internal.storage.dfs.DfsObjDatabase;
import org.eclipse.jgit.internal.storage.dfs.DfsRefDatabase;
import org.eclipse.jgit.internal.storage.dfs.DfsRepository;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryBuilder;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;

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
	private GitSharkDfsService dfsService;
	
	public GitSharkDfsRepository(final String id, final String repoName, final GitSharkDfsService dfsService) {
		this(new MongoRepositoryBuilder().setRepositoryDescription(new DfsRepositoryDescription(repoName)));
		this.id = id;
		this.dfsService = dfsService;
	}
	
	private GitSharkDfsRepository(final MongoRepositoryBuilder builder) {
		super(builder);
	}

	@Override
	public DfsObjDatabase getObjectDatabase() {
		return new GitSharkDfsObjDatabase(this, dfsService);
	}

	@Override
	public DfsRefDatabase getRefDatabase() {
		return new GitSharkDfsRefDatabase(this, dfsService);
	}

	public String getId() {
		return id;
	}
}
