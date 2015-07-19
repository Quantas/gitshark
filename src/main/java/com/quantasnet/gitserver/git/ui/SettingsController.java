package com.quantasnet.gitserver.git.ui;

import java.text.DecimalFormat;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.exception.GitServerErrorException;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.git.service.FilesystemRepositoryService;
import com.quantasnet.gitserver.git.service.RepoCacheService;

@RequestMapping("/repo/{repoOwner}/{repoName}/settings")
@Controller
public class SettingsController {

	@Autowired
	private FilesystemRepositoryService repoService;

	@Autowired
	private RepoCacheService repoCacheService;
	
	@RequestMapping(method = RequestMethod.GET)
	public String settings(final GitRepository repo, final Model model) {
		model.addAttribute("size", readableFileSize(repoService.repoSize(repo)));
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

	@RequestMapping(value = "/gc", method = RequestMethod.POST)
	public String gc(final GitRepository repo, final RedirectAttributes redirectAttributes) throws GitServerException {
		repo.execute(db -> {
			try {
				Git.wrap(db).gc().call();
				repoCacheService.clearCacheForRepo(repo);
				redirectAttributes.addFlashAttribute(Constants.SUCCESS_STATUS, "Garbage Collection Successful!");
			} catch (final GitAPIException e) {
				redirectAttributes.addFlashAttribute(Constants.FAILURE_STATUS, "Garbage Collection Failed!");
				throw new GitServerErrorException(e);
			}
		});
		return "redirect:/repo/" + repo.getInterfaceBaseUrl() + "/settings";
	}

	public static String readableFileSize(final long size) {
		if (size <= 0) {
			return "0";
		}

		final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
		final int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}
