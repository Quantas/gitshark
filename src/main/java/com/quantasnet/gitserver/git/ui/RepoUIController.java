package com.quantasnet.gitserver.git.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.LargeObjectException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.HandlerMapping;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.model.Breadcrumb;
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
	
	/**
	 * For browsing the tree and viewing files
	 */
	@RequestMapping({ "/{repoOwner}/{repoName}/tree", "/{repoOwner}/{repoName}/tree/**" })
	public String displayRepoTree(final GitRepository repo, @PathVariable final String repoOwner, @PathVariable final String repoName, @RequestParam(required = false) final boolean file, final Model model, final HttpServletRequest req) throws Exception {

		final String repoPath = "/repo/" + repoOwner + '/' + repoName + "/tree/";
		final String path = resolvePath(req, repoPath);
		
		model.addAttribute("breadcrumbs", Breadcrumb.generateBreadcrumbs(req.getContextPath(), repoName, repoPath, path));
		
		GitRepository.execute(repo, db -> {
			model.addAttribute("branches", db.getRefDatabase().getRefs("refs/heads/").keySet());
			
			if (file) {
				model.addAttribute("file", getFileToDisplay(repo, db, path));
			} else {
				final List<RepoFile> files = getFiles(repo, db, path, false);
				model.addAttribute("readme", resolveReadMeFile(db, files));
				model.addAttribute("files", files);
			}
		});
		
		if (file) {
			return "git/file";
		}
		
		return "git/single";
	}
	
	private String resolvePath(final HttpServletRequest req, final String repoPath) {
		String path = ((String) req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
		if (!path.endsWith("/")) {
			path = path + "/";
		}
		
		path = path.replaceAll(repoPath, "");
		
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		
		return path;
	}
	
	private RepoFile getFileToDisplay(final GitRepository repo, final Repository db, final String path) throws RevisionSyntaxException, MissingObjectException, IncorrectObjectTypeException, AmbiguousObjectException, IOException, GitAPIException {
		final List<RepoFile> files = getFiles(repo, db, path, true);
		if (!files.isEmpty()) {
			return files.get(0);
		}
		
		return null;
	}
	
	private List<RepoFile> getFiles(final GitRepository repo, final Repository db, final String path, final boolean file) throws RevisionSyntaxException, MissingObjectException, IncorrectObjectTypeException, AmbiguousObjectException, IOException, GitAPIException {
		final List<RepoFile> files = new ArrayList<>();
		
		try (final RevWalk revWalk = new RevWalk(db); final TreeWalk treeWalk = new TreeWalk(db)) {
		
			final RevCommit headCommit = revWalk.parseCommit(db.resolve(Constants.HEAD));
			
			treeWalk.addTree(headCommit.getTree());
			treeWalk.setRecursive(false);
			
			final boolean customPath = null != path && path.length() > 1; 
			
			if (customPath) {
				treeWalk.setFilter(PathFilter.create(path));
			}
			
			// if we are at root already, we can assume we are in the folder we want to be already
			boolean alreadyInside = !customPath;
			
			while (treeWalk.next()) {
				final String pathString = treeWalk.getPathString();
				final boolean directory = treeWalk.isSubtree();
				
				if (directory && customPath && !alreadyInside) {
					// if we aren't in the directory we want to be yet, go to the next one
					treeWalk.enterSubtree();
				}
	
				// If we found the folder we are looking for, set boolean and add dummy file
				if (pathString.equals(path)) {
					alreadyInside = true;
					
					// If we don't want a single file, we need a dummy file for navigating backwards
					if (!file) {
						// Add dummy file for navigating backwards
						files.add(buildBackwardsNavigationFile(repo, pathString));
						continue;
					}
				}
				
				// If we found the path we were looking for, start lining up the files
				if (alreadyInside) {
					final ObjectId objectId = treeWalk.getObjectId(0);
					final RepoFile repoFile = buildRepoFileObject(repo, db, path, treeWalk, customPath, pathString, directory, objectId); 
					files.add(repoFile);
					
					// if we wanted just a single file, get it's contents for display and get out of here
					if (file) {
						repoFile.setFileContents(getFileContents(db, objectId));
						break;
					}
				}
			}
		}
		
		Collections.sort(files);
		return files;
	}

	private RepoFile buildBackwardsNavigationFile(final GitRepository repo, final String pathString) {
		String parent;
		// If parent is not root, remove trailer, else set to blank
		if (pathString.indexOf("/") > 0) {
			parent = pathString.substring(0, pathString.lastIndexOf("/"));
		} else {
			parent = "";
		}
		
		// Add dummy file for navigating backwards
		return new RepoFile(repo, "", ". .", parent, true, 0, null, null);
	}
	
	private String resolveReadMeFile(final Repository db, final List<RepoFile> files) throws LargeObjectException, MissingObjectException, IOException {
		for (final RepoFile file : files) {
			if (isReadmeFile(file.getName())) {
				return getFileContents(db, ObjectId.fromString(file.getObjectId()));
			}
 		}
		return null;
	}
	
	private boolean isReadmeFile(final String path) {
		final String smallPath = path.toLowerCase();
		
		return "readme.md".equals(smallPath) || "readme.markdown".equals(smallPath); 
	}
	
	private RepoFile buildRepoFileObject(final GitRepository repo, final Repository db, final String path, final TreeWalk treeWalk, final boolean customPath, final String pathString, 
			final boolean directory, final ObjectId objectId) throws GitAPIException, NoHeadException {
		
		final String name = customPath ? pathString.replaceFirst(path + "/", "") : pathString;
		final String parent = pathString.substring(0, pathString.lastIndexOf("/") + 1);

		long size = 0;
		RevCommit commit = null;
		
		// WE only need commit and size information if this isn't a directory
		if (!directory) {
			try {
				size = treeWalk.getObjectReader().getObjectSize(objectId, 3); // 3 = BLOB
			} catch (Exception e) {
				size = 0;
			}
			
			try (final Git git = new Git(db)) {
				commit = git.log().addPath(pathString).setMaxCount(1).call().iterator().next();
			}
		}
		
		return new RepoFile(repo, name, parent, directory, size, objectId.getName(), commit);
	}
	
	private String getFileContents(final Repository db, final ObjectId objectId) throws LargeObjectException, MissingObjectException, IOException {
		return new String(db.newObjectReader().open(objectId).getBytes());
	}
}
