package com.quantasnet.gitserver.git.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.quantasnet.gitserver.git.model.Commit;
import com.quantasnet.gitserver.git.repo.GitRepository;


@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class SummaryController {

	@RequestMapping({ "", "/" })
	public String summary(final GitRepository repo, @PathVariable final String repoOwner, @PathVariable final String repoName, final Model model) throws Exception {
		
		GitRepository.execute(repo, db -> {
			
			final List<Commit> commits = new ArrayList<>();
			try (final Git git = new Git(db)) {
				git.log().setMaxCount(20).call().forEach(commit -> {
					commits.add(new Commit(commit));
				});
				
				model.addAttribute("commits", commits);
			} catch (Exception e) {
				
			}
		});
		
		return "git/summary";
		
	}
	
}
