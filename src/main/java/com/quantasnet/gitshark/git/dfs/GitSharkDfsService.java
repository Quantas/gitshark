package com.quantasnet.gitshark.git.dfs;

import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.internal.storage.dfs.DfsPackDescription;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.pack.PackExt;
import org.eclipse.jgit.lib.Ref;

import com.quantasnet.gitshark.git.exception.GitSharkErrorException;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.user.User;

public interface GitSharkDfsService {

	OutputStream getPackOutStream(DfsPackDescription desc, PackExt ext, String repoId);
	
	byte[] readFromPackFile(DfsPackDescription desc, PackExt ext, String repoId);
	
	List<DfsPackDescription> getPacks(String repoId, DfsRepositoryDescription desc);

	void deletePacks(Collection<DfsPackDescription> replaces, String repoId);
	
	long repositorySize(String repoId);
	
	GitSharkDfsRepo createRepo(String name, User user) throws GitSharkException;

	boolean deleteRepo(String name, User user);

	GitSharkDfsRepo getRepo(String name, String owner, String userName, User user) throws GitSharkErrorException;

	List<? extends GitSharkDfsRepo> getAllReposForUser(User user);

	List<? extends GitSharkDfsRef> getAllRefsForRepo(String repoId);

	GitSharkDfsRef getRefByNameForRepo(String name, String repoId);

	boolean updateRefByNameForRepo(String name, String repoId, Ref ref);

	boolean storeRefByNameForRepo(String name, String repoId, Ref ref);

	boolean deleteRefByNameForRepo(String name, String repoId);
}