package com.quantasnet.gitserver.git.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.model.RefLog;
import com.quantasnet.gitserver.git.repo.GitRepository;


@RequestMapping("/repo/{repoOwner}/{repoName}/reflog")
@Controller
public class RefLogController {

	@RequestMapping(value = "", method = RequestMethod.GET)
	public String branches(final GitRepository repo, final Model model) throws GitServerException {
		if (repo.hasCommits()) {
			repo.execute(db -> {
				final Set<String> branches = db.getRefDatabase().getRefs(Constants.REFS_HEADS).keySet();
				
				final List<RefLog> logs = new ArrayList<>();
				
				final Git git = Git.wrap(db);
				
				branches.forEach(branch -> {
					try {
						logs.addAll(git.reflog().setRef(branch).call()
							.stream()
							.map(reflog -> new RefLog(reflog, repo, db, branch))
							.collect(Collectors.toList()));
					} catch (Exception e) {
						// nothing here
					}
				});
				
				Collections.sort(logs);
				model.addAttribute("logs", logs);
			});
		}
		return "git/reflog";
	}
	
}
