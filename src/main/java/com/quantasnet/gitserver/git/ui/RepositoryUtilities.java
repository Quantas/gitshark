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
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import com.quantasnet.gitserver.git.model.RepoFile;
import com.quantasnet.gitserver.git.repo.GitRepository;

@Component
public class RepositoryUtilities {

	public  String resolvePath(final HttpServletRequest req, final String repoPath, final String branch) {
		String path = ((String) req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
		
		if (path.endsWith("/tree")) {
			path += "/" + branch + "/";
		}
		
		if (!path.endsWith("/")) {
			path = path + "/";
		}
		
		path = path.replaceAll(repoPath, "");
		
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		
		return path;
	}
	
	public RepoFile getFileToDisplay(final GitRepository repo, final Repository db, final String branch, final String path) throws RevisionSyntaxException, MissingObjectException, IncorrectObjectTypeException, AmbiguousObjectException, IOException, GitAPIException {
		final List<RepoFile> files = getFiles(repo, db, branch, path, true);
		if (!files.isEmpty()) {
			return files.get(0);
		}
		
		return null;
	}
	
	public List<RepoFile> getFiles(final GitRepository repo, final Repository db, final String branch, final String path, final boolean file) throws RevisionSyntaxException, MissingObjectException, IncorrectObjectTypeException, AmbiguousObjectException, IOException, GitAPIException {
		final List<RepoFile> files = new ArrayList<>();
		
		try (final RevWalk revWalk = new RevWalk(db); final TreeWalk treeWalk = new TreeWalk(db)) {
		
			final RevCommit headCommit = revWalk.parseCommit(db.resolve(branch));
			
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
						files.add(buildBackwardsNavigationFile(repo, pathString, branch));
						continue;
					}
				}
				
				// If we found the path we were looking for, start lining up the files
				if (alreadyInside) {
					final ObjectId objectId = treeWalk.getObjectId(0);
					final RepoFile repoFile = buildRepoFileObject(repo, db, path, branch, treeWalk, customPath, pathString, directory, objectId); 
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

	public RepoFile buildRepoFileObject(final GitRepository repo, final Repository db, final String path, final String branch, final TreeWalk treeWalk, final boolean customPath, final String pathString, 
			final boolean directory, final ObjectId objectId) throws GitAPIException, NoHeadException {
		
		final String name = customPath ? pathString.replaceFirst(path + "/", "") : pathString;
		final String parent = pathString.substring(0, pathString.lastIndexOf("/") + 1);

		RevCommit commit = null;
		
		// We only need commit information if this isn't a directory
		if (!directory) {
			try (final Git git = new Git(db)) {
				commit = git.log().add(db.resolve(branch)).addPath(pathString).setMaxCount(1).call().iterator().next();
			} catch (final RevisionSyntaxException | IOException e) {
				// Something horrible has probably happened, but we don't care really
			}
		}
		
		return new RepoFile(repo, name, parent, directory, branch, objectId.getName(), commit);
	}
	
	public String getFileContents(final Repository db, final ObjectId objectId) throws LargeObjectException, MissingObjectException, IOException {
		return new String(db.newObjectReader().open(objectId).getBytes());
	}
	
	private RepoFile buildBackwardsNavigationFile(final GitRepository repo, final String pathString, final String branch) {
		String parent;
		// If parent is not root, remove trailer, else set to blank
		if (pathString.indexOf("/") > 0) {
			parent = pathString.substring(0, pathString.lastIndexOf("/"));
		} else {
			parent = "";
		}
		
		// Add dummy file for navigating backwards
		return new RepoFile(repo, "", ". .", parent, true, branch, null, null);
	}
}
