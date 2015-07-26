package com.quantasnet.gitserver.git.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.git.service.RefLogService;


@RequestMapping("/repo/{repoOwner}/{repoName}/reflog")
@Controller
public class RefLogController {

	@Autowired
	private RefLogService refLogService;
	
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String branches(final GitRepository repo, final Model model) throws GitServerException {
		if (repo.hasCommits()) {
			model.addAttribute("logs", refLogService.retrieveActivity(repo));
		}
		return "git/reflog";
	}
	
}
