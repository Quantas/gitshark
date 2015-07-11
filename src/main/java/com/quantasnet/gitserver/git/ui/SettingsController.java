package com.quantasnet.gitserver.git.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.repo.FilesystemRepositoryService;
import com.quantasnet.gitserver.git.repo.GitRepository;

@RequestMapping("/repo/{repoOwner}/{repoName}/settings")
@Controller
public class SettingsController {

	@Autowired
	private FilesystemRepositoryService repoService;
	
	@RequestMapping(method = RequestMethod.GET)
	public String settings(final GitRepository repo) {
		return "git/settings";
	}
	
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public String deleteRepository(final GitRepository repo, final RedirectAttributes redirectAttributes) {
		if (repoService.deleteRepo(repo)) {
			redirectAttributes.addFlashAttribute(Constants.SUCCESS_STATUS, "Repository " + repo.getFullDisplayName() + " was successfully deleted.");
		} else {
			redirectAttributes.addFlashAttribute(Constants.FAILURE_STATUS, "Repository " + repo.getFullDisplayName() + " could not be deleted.");
		}
		
		return "redirect:/repo";
	}
	
}
