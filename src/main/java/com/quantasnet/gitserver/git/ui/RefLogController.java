package com.quantasnet.gitserver.git.ui;

import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.git.model.RefLog;
import com.quantasnet.gitserver.git.repo.GitRepository;


@RequestMapping("/repo/{repoOwner}/{repoName}/reflog")
@Controller
public class RefLogController {

	@RequestMapping(value = "", method = RequestMethod.GET)
	public String branches(final GitRepository repo, final Model model) throws Exception {
		if (repo.hasCommits()) {
			repo.execute(db -> {
				model.addAttribute("logs", Git.wrap(db).reflog().setRef(db.getBranch()).call()
						.stream()
						.map(reflog -> { return new RefLog(reflog, repo, db); })
						.collect(Collectors.toList()));
			});
		}
		return "git/reflog";
	}
	
}
