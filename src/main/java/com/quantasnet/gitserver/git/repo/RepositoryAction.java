package com.quantasnet.gitserver.git.repo;

import java.io.IOException;

import org.eclipse.jgit.lib.Repository;

import com.quantasnet.gitserver.git.exception.GitSharkException;

/**
 * To be used in conjunction with {@link GitRepository#execute(RepositoryAction)} to
 * perform an action against a JGit Repository.
 */
@FunctionalInterface
public interface RepositoryAction {

	/**
	 * Perform an action against a JGit Repository.
	 *
	 * @param repo
	 * @throws GitSharkException
	 * @throws IOException
	 */
	void doAction(Repository repo) throws GitSharkException, IOException;
}
