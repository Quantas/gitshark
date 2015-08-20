package com.quantasnet.gitshark.git.ui;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.quantasnet.gitshark.git.exception.GitSharkErrorException;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.model.Breadcrumb;
import com.quantasnet.gitshark.git.model.Commit;
import com.quantasnet.gitshark.git.model.RepoFile;
import com.quantasnet.gitshark.git.repo.GitRepository;
import com.quantasnet.gitshark.git.service.RepositoryUtilities;
import com.quantasnet.gitshark.git.ui.display.DisplayType;
import com.quantasnet.gitshark.git.ui.display.DisplayViewService;

@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class DisplayController {

	private static final Logger LOG = LoggerFactory.getLogger(DisplayController.class);
	
	@Autowired
	private RepositoryUtilities repoUtils;

	@Autowired
	private DisplayViewService displayViewService;

	@Autowired
	private ServletContext servletContext;
	
	@RequestMapping("/tree")
	public Object displayRepoTreeNoBranch(final GitRepository repo, final Model model, final HttpServletRequest req) throws GitSharkException {
		final String ref = repo.executeWithReturn(db -> {
			try {
				return db.getBranch();
			} catch (final Exception e) {
				LOG.trace("No HEAD found while trying to display tree", e);
				return null;
			}
		});
		return displayRepoTree(repo, DisplayType.TREE, ref, model, req);
	}
	
	@RequestMapping("/{type:(?:tree|raw|history)}/{ref}/**")
	public Object displayRepoTree(final GitRepository repo, @PathVariable final DisplayType type, @PathVariable final String ref, final Model model, final HttpServletRequest req) throws GitSharkException {
		final String repoPath = "/repo/" + repo.getInterfaceBaseUrl() + "/tree/" + ref + '/';
		final String path = repoUtils.resolvePath(req, repoPath, ref);
		
		populateBreadcrumbs(repo, type, ref, model, repoPath, path);
		
		return repo.executeWithReturn(db -> {
			if (repo.hasCommits()) {
				repoUtils.addRefsToModel(model, repo);
				
				final RevCommit commit = repoUtils.getRefHeadCommit(ref, repo, db);
				model.addAttribute("lastCommit", new Commit(commit, repo));

				final List<RepoFile> files = repoUtils.getFiles(repo, db, ref, path);

				final DisplayType actualType;

				// sort of a hack to check for file
				if (type == DisplayType.TREE && files.size() == 1 && !files.get(0).isDirectory()) {
					actualType = DisplayType.FILE;
				} else {
					actualType = type;
				}

				return displayViewService.getForType(actualType).display(repo, ref, model, path, db, files);
			}

			throw new GitSharkErrorException("error in tree rendering");
		});
	}

	private void populateBreadcrumbs(final GitRepository repo, final DisplayType type, final String ref, final Model model, final String repoPath, final String path) {
		final String breadCrumbsPath = DisplayType.HISTORY == type ? repoPath.replaceFirst("\\/tree\\/", "/history/") : repoPath;

		model.addAttribute("branch", ref);
		model.addAttribute("breadcrumbs", Breadcrumb.generateBreadcrumbs(servletContext.getContextPath(), repo.getDisplayName(), breadCrumbsPath, path));
	}

}