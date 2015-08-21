package com.quantasnet.gitshark.git.ui;

import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.quantasnet.gitshark.Constants;
import com.quantasnet.gitshark.git.cache.EvictAllCaches;
import com.quantasnet.gitshark.git.cache.EvictRepoCache;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.repo.GitRepository;
import com.quantasnet.gitshark.git.service.RefService;

@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class BranchCreateController {

	private static final Logger LOG = LoggerFactory.getLogger(BranchCreateController.class);
	
	@Autowired
	private RefService refService;
	
	@RequestMapping(value = "/branch/create", method = RequestMethod.GET)
	public String create(final GitRepository repo, final Model model) throws GitSharkException {
		model.addAttribute("branches", refService.branches(repo).keySet());
		return "git/branchcreate";
	}
	
	@EvictAllCaches
	@EvictRepoCache
	@RequestMapping(value = "/branch/create", method = RequestMethod.POST)
	public String saveNewBranch(final GitRepository repo, @RequestParam final String sourceBranch, @RequestParam final String newBranch, final RedirectAttributes redirectAttributes) throws GitSharkException {
		
		repo.execute(db -> {
			final Set<String> branches = refService.branches(repo).keySet();
			if (branches.contains(sourceBranch) && !branches.contains(newBranch)) {
				try {
					Git.wrap(db)
						.branchCreate()
						.setStartPoint(sourceBranch)
						.setName(newBranch)
						.call();
				} catch (final GitAPIException e) {
					LOG.debug("Error creating branch from UI", e);
					addBranchCreateError(redirectAttributes);
				}
				redirectAttributes.addFlashAttribute(Constants.SUCCESS_STATUS, "Branch " + newBranch + " was successfully created");
			} else {
				addBranchCreateError(redirectAttributes);
			}
		});
		
		return "redirect:/repo/" + repo.getInterfaceBaseUrl() + "/branch";
	}
	
	private void addBranchCreateError(final RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute(Constants.FAILURE_STATUS, "Either the source branch is missing or the new branch already exists.");
	}
}
