package com.quantasnet.gitserver.git.repo;

import org.eclipse.jgit.lib.Repository;

@FunctionalInterface
public interface RepositoryAction {
	void doAction(final Repository repo) throws Exception;
}
