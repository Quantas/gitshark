package com.quantasnet.gitserver.git.ui.display;

import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.springframework.ui.Model;

import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.model.RepoFile;
import com.quantasnet.gitserver.git.repo.GitRepository;

/**
 * Used to generate views for the DisplayController
 */
public interface DisplayView {

	/**
	 * Display the view defined by this type
	 *
	 * @param repo {@link GitRepository}
	 * @param ref Ref to display (branch name, tag name, commit hash)
	 * @param model {@link Model}
	 * @param path Current Repo path
	 * @param db {@link Repository}
	 * @param files Current list of files
	 * @return View Name or other valid MVC method return type
	 * @throws GitServerException
	 */
	Object display(GitRepository repo, String ref, Model model, String path, Repository db, List<RepoFile> files) throws GitServerException;

	/**
	 * @return the {@link DisplayType} this DisplayView is for
	 */
	DisplayType getType();

}