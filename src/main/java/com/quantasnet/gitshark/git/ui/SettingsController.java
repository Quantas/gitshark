package com.quantasnet.gitshark.git.ui;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.quantasnet.gitshark.Constants;
import com.quantasnet.gitshark.git.cache.EvictRepoCache;
import com.quantasnet.gitshark.git.dfs.mongo.MongoService;
import com.quantasnet.gitshark.git.exception.GitSharkErrorException;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.repo.GitRepository;
import com.quantasnet.gitshark.git.service.CommitService;
import com.quantasnet.gitshark.git.service.FilesystemRepositoryService;
import com.quantasnet.gitshark.user.User;

@RequestMapping("/repo/{repoOwner}/{repoName}/settings")
@Controller
public class SettingsController {

	@Autowired
	private FilesystemRepositoryService repoService;
	
	@Autowired
	private MongoService mongoService;

	@Autowired
	private CommitService commitService;

	@RequestMapping(method = RequestMethod.GET)
	public String settings(final GitRepository repo, final Model model) throws GitSharkException {
		if (repo.hasCommits()) {
			model.addAttribute("commitCount", commitService.commitCount(repo));
			model.addAttribute("branchCount", repoService.branches(repo).size());
			model.addAttribute("tagCount", repoService.tags(repo).size());
		}
		return "git/settings";
	}
	
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public String deleteRepository(final GitRepository repo, @AuthenticationPrincipal final User user, final RedirectAttributes redirectAttributes) {
		if (mongoService.deleteRepo(repo.getName(), user)) {
			redirectAttributes.addFlashAttribute(Constants.SUCCESS_STATUS, "Repository " + repo.getFullDisplayName() + " was successfully deleted.");
		} else {
			redirectAttributes.addFlashAttribute(Constants.FAILURE_STATUS, "Repository " + repo.getFullDisplayName() + " could not be deleted.");
		}
		
		return "redirect:/repo";
	}

	@EvictRepoCache
	@RequestMapping(value = "/gc", method = RequestMethod.POST)
	public String gc(final GitRepository repo, final RedirectAttributes redirectAttributes) throws GitSharkException {
		repo.execute(db -> {
			try {
				Git.wrap(db).gc().call();
				redirectAttributes.addFlashAttribute(Constants.SUCCESS_STATUS, "Garbage Collection Successful!");
			} catch (final GitAPIException e) {
				redirectAttributes.addFlashAttribute(Constants.FAILURE_STATUS, "Garbage Collection Failed!");
				throw new GitSharkErrorException(e);
			}
		});
		return "redirect:/repo/" + repo.getInterfaceBaseUrl() + "/settings";
	}
}