package com.quantasnet.gitserver.git.ui;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.model.Breadcrumb;
import com.quantasnet.gitserver.git.model.Commit;
import com.quantasnet.gitserver.git.model.RepoFile;
import com.quantasnet.gitserver.git.repo.GitRepository;

@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class TreeController {

	@Autowired
	private RepositoryUtilities repoUtils;
	
	@Autowired
	private ReadmeFileService readmeService;
	
	@RequestMapping("/tree")
	public String displayRepoTreeNoBranch(final GitRepository repo, @PathVariable final String repoOwner, @PathVariable final String repoName, final Model model, final HttpServletRequest req) throws Exception {
		final StringBuilder builder = new StringBuilder();
		repo.execute(db -> {
			builder.append(db.getBranch());
		});
		return displayRepoTree(repo, repoOwner, repoName, builder.toString(), false, model, req);
	}
	
	@RequestMapping("/tree/{branch}/**")
	public String displayRepoTree(final GitRepository repo, @PathVariable final String repoOwner, @PathVariable final String repoName, @PathVariable final String branch, @RequestParam(required = false) final boolean file, final Model model, final HttpServletRequest req) throws Exception {
		final String repoPath = "/repo/" + repoOwner + '/' + repoName + "/tree/" + branch + '/';
		final String path = repoUtils.resolvePath(req, repoPath, branch);
		
		model.addAttribute("branch", branch);
		model.addAttribute("breadcrumbs", Breadcrumb.generateBreadcrumbs(req.getContextPath(), repoName, repoPath, path));
		
		repo.execute(db -> {
			if (repo.hasCommits()) {
				try {
					final RevCommit commit = Git.wrap(db).log().add(db.resolve(branch)).setMaxCount(1).call().iterator().next();
					model.addAttribute("lastCommit", new Commit(commit, repo));
				} catch (final Exception e) {
					
				}
				
				model.addAttribute("branches", db.getRefDatabase().getRefs(Constants.REFS_HEADS).keySet());
				if (file) {
					model.addAttribute("file", repoUtils.getFileToDisplay(repo, db, branch, path));
				} else {
					final List<RepoFile> files = repoUtils.getFiles(repo, db, branch, path, false);
					model.addAttribute("readme", readmeService.resolveReadMeFile(repo, db, files));
					model.addAttribute("files", files);
				}
			}
		});
		
		if (file) {
			return "git/file";
		}
		return "git/tree";
	}
}