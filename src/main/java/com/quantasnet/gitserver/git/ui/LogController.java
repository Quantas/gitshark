package com.quantasnet.gitserver.git.ui;

import static org.eclipse.jgit.lib.RefDatabase.ALL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.quantasnet.gitserver.git.model.Commit;
import com.quantasnet.gitserver.git.repo.GitRepository;


@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class LogController {

	@RequestMapping("/log")
	public String showLog(final GitRepository repo, @PathVariable final String repoOwner, @PathVariable final String repoName, final Model model) throws Exception {
		repo.execute(db -> {
			if (repo.isHasCommits()) {
				final int maxCount = 20;
				final List<Commit> commits = new ArrayList<>();
				
				try (final RevWalk revWalk = new RevWalk(db)) {
					final List<RevCommit> headCommits = new ArrayList<>();
					final Map<String, Ref> refs = db.getRefDatabase().getRefs(ALL);
					for (Ref ref : refs.values()) {
						if(!ref.isPeeled()) {
							ref = db.peel(ref);
						}
						
						ObjectId objectId = ref.getPeeledObjectId();
						if (null == objectId) {
							objectId = ref.getObjectId();
						}
						RevCommit commit = null;
						try {
							commit = revWalk.parseCommit(objectId);
						} catch (MissingObjectException | IncorrectObjectTypeException e) {
						}
						if (commit != null) {
							headCommits.add(commit);
						}
					}
					revWalk.markStart(headCommits);
					
					for (final RevCommit rev : revWalk) {
						commits.add(new Commit(rev));
						if (commits.size() == maxCount) {
							break;
						}
					}
					model.addAttribute("commits", commits);
				} catch (Exception e) {
					
				}
			}
		});
		return "git/log";
	}
}
