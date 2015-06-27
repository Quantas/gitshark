package com.quantasnet.gitserver.git.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.repo.GitRepository;

@RequestMapping("/repo/{repoOwner}/{repoName}/tag")
@Controller
public class TagController {

	@RequestMapping(value = "", method = RequestMethod.GET)
	public String branches(final GitRepository repo, final Model model) throws Exception {
		
		repo.execute(db -> {
			model.addAttribute("tags", db.getRefDatabase().getRefs(Constants.REFS_TAGS).keySet());
		});
		
		return "git/tags";
	}
	
}
