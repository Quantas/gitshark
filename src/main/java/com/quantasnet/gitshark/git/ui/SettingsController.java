package com.quantasnet.gitshark.git.ui;

import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.quantasnet.gitshark.Constants;
import com.quantasnet.gitshark.git.cache.EvictAllCaches;
import com.quantasnet.gitshark.git.cache.EvictRepoCache;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsService;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.repo.GitRepository;
import com.quantasnet.gitshark.user.User;

@RequestMapping("/repo/{repoOwner}/{repoName}/settings")
@Controller
public class SettingsController {

	private static final Logger LOG = LoggerFactory.getLogger(SettingsController.class);
	
	@Autowired
	private GitSharkDfsService dfsService;

	@RequestMapping(method = RequestMethod.GET)
	public String settings(final GitRepository repo, final Model model) throws GitSharkException {
		if (repo.hasCommits()) {
			model.addAttribute("files", dfsService.getPacks(repo.getId(), new DfsRepositoryDescription(repo.getName())));
		}
		return "git/settings";
	}

	@EvictAllCaches
	@EvictRepoCache
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public String deleteRepository(final GitRepository repo, @AuthenticationPrincipal final User user, final RedirectAttributes redirectAttributes) {
		if (dfsService.deleteRepo(repo.getName(), user)) {
			redirectAttributes.addFlashAttribute(Constants.SUCCESS_STATUS, "Repository " + repo.getFullDisplayName() + " was successfully deleted.");
		} else {
			redirectAttributes.addFlashAttribute(Constants.FAILURE_STATUS, "Repository " + repo.getFullDisplayName() + " could not be deleted.");
		}
		
		return "redirect:/repo";
	}
}