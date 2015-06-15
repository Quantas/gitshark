package com.quantasnet.gitserver.git.ui;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quantasnet.gitserver.git.model.Breadcrumb;
import com.quantasnet.gitserver.git.model.RepoFile;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.git.repo.RepositoryService;

@RequestMapping("/repo")
@Controller
public class RepoUIController {

	@Autowired
	private RepositoryService repoService;
	
	@Autowired
	private RepositoryUtilities repoUtils;
	
	@RequestMapping
	public String myRepos(@AuthenticationPrincipal final User user, final Model model) {
		model.addAttribute("repos", repoService.getRepositories(user.getUsername()));
		return "git/list";
	}
	
	@RequestMapping("/create/{repoName}")
	public String createRepo(@AuthenticationPrincipal final User user, @PathVariable final String repoName) throws IOException {
		repoService.createRepo(repoName, user.getUsername());
		return "redirect:/repo/";
	}
	
	@RequestMapping("/{repoOwner}/{repoName}/tree")
	public String displayRepoTreeNoBranch(final GitRepository repo, @PathVariable final String repoOwner, @PathVariable final String repoName, final Model model, final HttpServletRequest req) throws Exception {
		final StringBuilder builder = new StringBuilder();
		GitRepository.execute(repo, db -> {
			builder.append(db.getBranch());
		});
		return displayRepoTree(repo, repoOwner, repoName, builder.toString(), false, model, req);
	}
	
	@RequestMapping("/{repoOwner}/{repoName}/tree/{branch}/**")
	public String displayRepoTree(final GitRepository repo, @PathVariable final String repoOwner, @PathVariable final String repoName, @PathVariable final String branch, @RequestParam(required = false) final boolean file, final Model model, final HttpServletRequest req) throws Exception {
		final String repoPath = "/repo/" + repoOwner + '/' + repoName + "/tree/" + branch + '/';
		final String path = repoUtils.resolvePath(req, repoPath, branch);
		
		model.addAttribute("branch", branch);
		model.addAttribute("breadcrumbs", Breadcrumb.generateBreadcrumbs(req.getContextPath(), repoName, repoPath, path));
		
		GitRepository.execute(repo, db -> {
			final boolean hasCommits = GitRepository.hasCommits(db);
			model.addAttribute("hasCommits", hasCommits);
			if (hasCommits) {
				model.addAttribute("branches", db.getRefDatabase().getRefs("refs/heads/").keySet());
				if (file) {
					model.addAttribute("file", repoUtils.getFileToDisplay(repo, db, branch, path));
				} else {
					final List<RepoFile> files = repoUtils.getFiles(repo, db, branch, path, false);
					model.addAttribute("readme", repoUtils.resolveReadMeFile(repo, db, files));
					model.addAttribute("files", files);
				}
			}
		});
		
		if (file) {
			return "git/file";
		}
		return "git/repo";
	}
}