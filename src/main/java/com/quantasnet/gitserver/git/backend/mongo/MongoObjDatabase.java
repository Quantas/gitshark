package com.quantasnet.gitserver.git.backend.mongo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.internal.storage.dfs.DfsObjDatabase;
import org.eclipse.jgit.internal.storage.dfs.DfsOutputStream;
import org.eclipse.jgit.internal.storage.dfs.DfsPackDescription;
import org.eclipse.jgit.internal.storage.dfs.DfsReaderOptions;
import org.eclipse.jgit.internal.storage.dfs.DfsRepository;
import org.eclipse.jgit.internal.storage.dfs.ReadableChannel;
import org.eclipse.jgit.internal.storage.pack.PackExt;

public class MongoObjDatabase extends DfsObjDatabase {

	MongoObjDatabase(DfsRepository repository) {
		super(repository, new DfsReaderOptions());
	}

	@Override
	protected DfsPackDescription newPack(PackSource source) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void commitPackImpl(Collection<DfsPackDescription> desc, Collection<DfsPackDescription> replaces)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void rollbackPack(Collection<DfsPackDescription> desc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected List<DfsPackDescription> listPacks() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ReadableChannel openFile(DfsPackDescription desc, PackExt ext)
			throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DfsOutputStream writeFile(DfsPackDescription desc, PackExt ext) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
}