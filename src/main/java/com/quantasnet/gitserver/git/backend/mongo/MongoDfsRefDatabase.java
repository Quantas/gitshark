package com.quantasnet.gitserver.git.backend.mongo;

import java.io.IOException;

import org.eclipse.jgit.internal.storage.dfs.DfsRefDatabase;
import org.eclipse.jgit.internal.storage.dfs.DfsRepository;
import org.eclipse.jgit.lib.Ref;

public class MongoDfsRefDatabase extends DfsRefDatabase {

	public MongoDfsRefDatabase(DfsRepository repository) {
		super(repository);
	}

	@Override
	protected RefCache scanAllRefs() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean compareAndPut(Ref oldRef, Ref newRef) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean compareAndRemove(Ref oldRef) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

}
