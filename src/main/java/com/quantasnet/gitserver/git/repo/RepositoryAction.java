package com.quantasnet.gitserver.git.repo;

import java.io.IOException;

import org.eclipse.jgit.lib.Repository;

import com.quantasnet.gitserver.git.exception.GitServerException;

@FunctionalInterface
public interface RepositoryAction {
	void doAction(final Repository repo) throws GitServerException, IOException;
}
