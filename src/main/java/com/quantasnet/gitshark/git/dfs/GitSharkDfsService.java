package com.quantasnet.gitshark.git.dfs;

import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.internal.storage.dfs.DfsPackDescription;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.pack.PackExt;
import org.eclipse.jgit.lib.Ref;

import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.exception.RepositoryNotFoundException;
import com.quantasnet.gitshark.user.User;

public interface GitSharkDfsService {

	OutputStream getPackOutStream(DfsPackDescription desc, PackExt ext, String repoId);
	byte[] readFromPackFile(DfsPackDescription desc, PackExt ext, String repoId);
	List<DfsPackDescription> getPacks(String repoId, DfsRepositoryDescription desc);
	void deletePacks(Collection<DfsPackDescription> replaces, String repoId);
	
	long repositorySize(String repoId);
	
	GitSharkDfsRepo createRepo(String name, User user) throws GitSharkException;
	boolean deleteRepo(String name, User user);
	GitSharkDfsRepo getRepo(String name, String owner) throws RepositoryNotFoundException;
	List<? extends GitSharkDfsRepo> getAllReposForUser(User user);
	List<? extends GitSharkDfsRef> getAllRefsForRepo(String repoId);

	GitSharkRepoSecurity getSecurityForRepo(String repoId);
	void saveSecurityForRepo(GitSharkRepoSecurity security);

	GitSharkDfsRef getRefByNameForRepo(String name, String repoId);
	boolean updateRefByNameForRepo(String name, String repoId, Ref ref);
	boolean storeRefByNameForRepo(String name, String repoId, Ref ref);
	boolean deleteRefByNameForRepo(String name, String repoId);
	
	GitSharkDfsRefLog createEmptyRefLog();
	void saveRefLog(GitSharkDfsRefLog refLog);
	List<? extends GitSharkDfsRefLog> getRefLogForRepo(String repoId);
}