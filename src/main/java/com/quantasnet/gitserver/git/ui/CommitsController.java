package com.quantasnet.gitserver.git.ui;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.quantasnet.gitserver.git.exception.GitSharkErrorException;
import com.quantasnet.gitserver.git.exception.GitSharkException;
import com.quantasnet.gitserver.git.model.Commit;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.git.service.CommitService;
import com.quantasnet.gitserver.git.service.RepositoryUtilities;

/**
 * @author andrewlandsverk
 */
@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class CommitsController {

	@Autowired
	private RepositoryUtilities repoUtils;

	@Autowired
	private CommitService commitService;

	@RequestMapping("/commits")
	public String showLog(final GitRepository repo, @RequestParam(required = false) final String selected, final Model model) throws GitSharkException {
		repo.execute(db -> {
			if (repo.hasCommits()) {
				repoUtils.addRefsToModel(model, repo);
				
				if (null != selected) {
					model.addAttribute("branch", selected);
				}

				model.addAttribute("commits", commitService.getCommits(repo, selected, db));
			}
		});
		return "git/log";
	}
	
	@RequestMapping(value = "/commit/{commitId}")
	public String singleCommit(final GitRepository repo, @PathVariable final String commitId, final Model model) throws GitSharkException {
		if (repo.hasCommits()) {
			repo.execute(db -> {
				try (final RevWalk revWalk = new RevWalk(db)) {
					final RevCommit commit = revWalk.parseCommit(ObjectId.fromString(commitId));
					RevCommit parent;
					
					if (commit.getParentCount() > 0) {
						parent = revWalk.parseCommit(commit.getParent(0).getId());
					} else {
						parent = null;
					}

		            model.addAttribute("diffs", commitService.getDiffsForCommmit(repo, db, parent, commit, revWalk));
		            model.addAttribute("commit", new Commit(commit, repo));
				} catch (final IllegalArgumentException e) {
					throw new GitSharkException(e);
	            } catch (final GitAPIException e) {
            		throw new GitSharkErrorException(e);
            	}
			});
		}
		
		return "git/commit";
	}
}
