package com.quantasnet.gitserver.git.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.repo.GitRepository;

@RequestMapping("/repo/{repoOwner}/{repoName}/branch")
@Controller
public class BranchController {

	@RequestMapping(value = "", method = RequestMethod.GET)
	public String branches(final GitRepository repo, final Model model) throws Exception {
		
		repo.execute(db -> {
			model.addAttribute("branches", db.getRefDatabase().getRefs(Constants.REFS_HEADS).keySet());
		});
		
		return "git/branches";
	}
	
}
