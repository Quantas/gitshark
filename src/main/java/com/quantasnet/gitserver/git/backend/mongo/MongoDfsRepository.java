package com.quantasnet.gitserver.git.backend.mongo;

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
public class MongoDfsRepository extends DfsRepository {

	private static class MongoRepositoryBuilder extends DfsRepositoryBuilder<MongoRepositoryBuilder, MongoDfsRepository> {
		@Override
		public MongoDfsRepository build() throws IOException {
			return new MongoDfsRepository(this);
		}
	}
	
	public MongoDfsRepository(final String repoName) {
		this(new MongoRepositoryBuilder().setRepositoryDescription(new DfsRepositoryDescription(repoName)));
	}
	
	private MongoDfsRepository(final MongoRepositoryBuilder builder) {
		super(builder);
	}

	@Override
	public DfsObjDatabase getObjectDatabase() {
		return null;
	}

	@Override
	public DfsRefDatabase getRefDatabase() {
		return null;
	}	
}
