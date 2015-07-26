package com.quantasnet.gitserver.git.repo;

import java.io.IOException;

import org.eclipse.jgit.lib.Repository;

import com.quantasnet.gitserver.git.exception.GitServerException;

/**
 * To be used in conjunction with {@link GitRepository#executeWithReturn(RepositoryActionWithReturn)}
 * to perform an action against a JGit Repository.
 */
@FunctionalInterface
public interface RepositoryActionWithReturn<T> {

	/**
	 * Perform an action against a JGit Repository.
	 *
	 * @param repo
	 * @return T
	 * @throws GitServerException
	 * @throws IOException
	 */
	T doAction(Repository repo) throws GitServerException, IOException;
}
