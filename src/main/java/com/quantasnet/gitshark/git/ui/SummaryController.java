package com.quantasnet.gitshark.git.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.quantasnet.gitshark.Utils;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsService;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.repo.GitRepository;
import com.quantasnet.gitshark.git.service.CommitService;
import com.quantasnet.gitshark.git.service.RefService;
import com.quantasnet.gitshark.git.service.SpecialMarkupService;

@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class SummaryController {

	@Autowired
	private RefService refService;

	@Autowired
	private GitSharkDfsService dfsService;

	@Autowired
	private CommitService commitService;

	@Autowired
	private SpecialMarkupService specialMarkupService;
	
	@RequestMapping({ "", "/" })
	public String summary(final GitRepository repo, final Model model) throws GitSharkException {
		if (repo.hasCommits()) {
			model.addAttribute("repoSize", Utils.readableFileSize(dfsService.repositorySize(repo.getId())));
			model.addAttribute("commitCount", commitService.commitCount(repo));
			model.addAttribute("contributorCount", commitService.contributorCount(repo));
			model.addAttribute("branchCount", refService.branches(repo).size());
			model.addAttribute("tagCount", refService.tags(repo).size());

			repo.execute(db -> {
				model.addAttribute("specialmarkup", specialMarkupService.resolveReadMeFile(repo, db, db.getBranch()));
			});
		}
		
		return "git/summary";
	}
}