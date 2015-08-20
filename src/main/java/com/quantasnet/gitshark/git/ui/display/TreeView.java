package com.quantasnet.gitshark.git.ui.display;

import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.quantasnet.gitshark.git.exception.GitSharkErrorException;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.model.ReadmeFile;
import com.quantasnet.gitshark.git.model.RepoFile;
import com.quantasnet.gitshark.git.repo.GitRepository;
import com.quantasnet.gitshark.git.service.RepositoryUtilities;
import com.quantasnet.gitshark.git.service.SpecialMarkupService;

@Component
class TreeView implements DisplayView {

	private static final String SPECIAL_MARKUP = "specialmarkup";

	@Autowired
	private SpecialMarkupService specialMarkupService;

	@Autowired
	private RepositoryUtilities repoUtils;

	@Override
	public Object display(final GitRepository repo, final String ref, final Model model, final String path, final Repository db, final List<RepoFile> files) throws GitSharkException {
		if (repoUtils.isPathRoot(path)) {
			model.addAttribute(SPECIAL_MARKUP, specialMarkupService.resolveReadMeFile(repo, db, ref, files));
		} else {
			addReadmeIfExists(repo, ref, model, db, files);
		}

		model.addAttribute("files", files);
		return "git/tree";
	}

	@Override
	public DisplayType getType() {
		return DisplayType.TREE;
	}

	private void addReadmeIfExists(final GitRepository repo, final String ref, final Model model, final Repository db, final List<RepoFile> files) throws GitSharkErrorException {
		for (final RepoFile aFile : files) {
			if (!aFile.isDirectory() && specialMarkupService.isReadmeFile(aFile.getName())) {
				model.addAttribute(SPECIAL_MARKUP,
						new ReadmeFile(aFile.getName(), specialMarkupService.retrieveMarkup(repo, db, aFile, ref)));
				break;
			}
		}
	}
}
