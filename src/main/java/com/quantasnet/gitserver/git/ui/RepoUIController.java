package com.quantasnet.gitserver.git.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.model.RepoFile;
import com.quantasnet.gitserver.git.repo.GitRepository;
import com.quantasnet.gitserver.git.repo.RepositoryService;

@RequestMapping("/repo")
@Controller
public class RepoUIController {

	@Autowired
	private RepositoryService repoService;
	
	@RequestMapping
	public String myRepos(@AuthenticationPrincipal final User user, final Model model) {
		model.addAttribute("repos", repoService.getRepositories(user.getUsername()));
		return "git/list";
	}
	
	@RequestMapping("/create/{repoName}")
	public String createRepo(@AuthenticationPrincipal final User user, @PathVariable final String repoName) throws IOException {
		repoService.createRepo(repoName, user.getUsername());
		return "redirect:/repo/";
	}
	
	@RequestMapping("/{repoOwner}/{repoName}")
	public String displayRepo(@AuthenticationPrincipal final User user, final GitRepository repo, final Model model) throws Exception {

		GitRepository.execute(repo, db -> {
			final RevWalk revWalk = new RevWalk(db);
			final TreeWalk treeWalk = new TreeWalk(db);
			
			final boolean commits = GitRepository.hasCommits(db);
			
			model.addAttribute("repo", repo);
			model.addAttribute("commits", commits);
			
			if (commits) {
				
				final RevCommit headCommit = revWalk.parseCommit(db.resolve(Constants.HEAD));
				
				treeWalk.addTree(headCommit.getTree());
				treeWalk.setRecursive(false);
				
				final List<RepoFile> files = new ArrayList<>();
				while (treeWalk.next()) {
					final boolean directory = treeWalk.isSubtree();
					final ObjectId objectId = treeWalk.getObjectId(0);
					
					long size = 0;
					
					if (!directory) {
						try {
							size = treeWalk.getObjectReader().getObjectSize(objectId, 3); // 3 = BLOB
						} catch (Exception e) {
							
						}
					}
					
					files.add(new RepoFile(treeWalk.getPathString(), directory, size, objectId.getName()));
				}
				
				Collections.sort(files);
				
				model.addAttribute("files", files);
				
				model.addAttribute("branches", db.getRefDatabase().getRefs("refs/heads/").keySet());
			}
			
			treeWalk.close();
			revWalk.close();
		});
		
		return "git/single";
	}
	
	@RequestMapping(value = "/{repoOwner}/{repoName}/blob/{objectId}", method = RequestMethod.GET)
	public String showFile(final GitRepository repo, @PathVariable final String objectId, final Model model) throws IOException, Exception {

		model.addAttribute("repo", repo);
		
		GitRepository.execute(repo, db -> {
			try {
				final ObjectId objId = ObjectId.fromString(objectId);
				model.addAttribute("fileString", new String(db.newObjectReader().open(objId).getBytes()));
				model.addAttribute("found", Boolean.TRUE);
			} catch (final MissingObjectException | IllegalArgumentException e) {
				model.addAttribute("found", Boolean.FALSE);
			} catch (final LargeObjectException e) {
				model.addAttribute("display", Boolean.FALSE);
			}
		});
		
		return "git/file";
	}
}
