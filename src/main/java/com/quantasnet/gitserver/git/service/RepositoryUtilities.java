package com.quantasnet.gitserver.git.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.MaxCountRevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.HandlerMapping;

import com.quantasnet.gitserver.Constants;
import com.quantasnet.gitserver.git.exception.CommitNotFoundException;
import com.quantasnet.gitserver.git.exception.GitServerErrorException;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.git.model.RepoFile;
import com.quantasnet.gitserver.git.repo.GitRepository;

@Service
public class RepositoryUtilities {

	private static final Logger LOG = LoggerFactory.getLogger(RepositoryUtilities.class);

	public  String resolvePath(final HttpServletRequest req, final String repoPath, final String branch) {
		String path = (String) req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		
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

	@Cacheable(cacheNames = "hasCommits", key = "#repo.fullDisplayName")
	public boolean hasCommits(final GitRepository repo) throws GitServerException {
		return repo.executeWithReturn(db -> {
			if (db != null && db.getDirectory().exists()) {
				return (new File(db.getDirectory(), "objects").list().length > 2)
						|| (new File(db.getDirectory(), "objects/pack").list().length > 0);
			}
			return false;
		});
	}

	public RepoFile getFileToDisplay(final GitRepository repo, final Repository db, final String branch, final String path) throws GitServerException {
		final List<RepoFile> files = getFiles(repo, db, branch, path, true);
		if (!files.isEmpty()) {
			return files.get(0);
		}
		
		return null;
	}
	
	public List<RepoFile> getFiles(final GitRepository repo, final Repository db, final String branch, final String path, final boolean file) throws GitServerException {
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
					final RepoFile repoFile = buildRepoFileObject(repo, db, path, branch, customPath, pathString, directory, objectId); 
					files.add(repoFile);
					
					// if we wanted just a single file, get it's contents for display and get out of here
					if (file) {
						repoFile.setFileContentsRaw(getFileContents(db, objectId));
						break;
					}
				}
			}
		} catch (final IOException | GitAPIException e) {
			throw new GitServerErrorException(e);
		}
		
		Collections.sort(files);
		return files;
	}

	public RepoFile buildRepoFileObject(final GitRepository repo, final Repository db, final String path, final String ref, final boolean customPath, final String pathString, 
			final boolean directory, final ObjectId objectId) throws GitAPIException, CommitNotFoundException {
		
		final String name = customPath ? pathString.replaceFirst(path + "/", "") : pathString;
		final String parent = pathString.substring(0, pathString.lastIndexOf("/") + 1);

		RevCommit commit = null;
		
		// We only need commit information if this isn't a directory
		if (!directory) {
			try (final RevWalk walk = new RevWalk(db)) {
				walk.setTreeFilter(AndTreeFilter.create(
						PathFilterGroup.create(Collections.singletonList(PathFilter.create(pathString))),
						TreeFilter.ANY_DIFF));
				walk.setRevFilter(MaxCountRevFilter.create(1));
				walk.markStart(getRefHeadCommit(ref, db));
				
				commit = walk.iterator().next();
			} catch (final RevisionSyntaxException | IOException e) {
				LOG.trace("Error building RepoFile", e);
				// Something horrible has probably happened, but we don't care really
			}
		}
		
		return new RepoFile(repo, name, parent, directory, ref, objectId.getName(), commit);
	}
	
	public byte[] getFileContents(final Repository db, final ObjectId objectId) throws IOException {
		return db.newObjectReader().open(objectId).getBytes();
	}
	
	public RevCommit getRefHeadCommit(final String refString, final Repository db) throws IOException, CommitNotFoundException {
		final Ref branchRef = db.getRefDatabase().getRefs(Constants.REFS_HEADS).get(refString);
		final Ref tagRef = db.getRefDatabase().getRefs(Constants.REFS_TAGS).get(refString);		
		final Ref ref = branchRef != null ? branchRef : tagRef != null ? tagRef : null;

		// must be a commit id and not a ref
		if (null == ref) {
			try (final RevWalk revWalk = new RevWalk(db)) {
				return revWalk.parseCommit(ObjectId.fromString(refString));
			} catch (final Exception e) {
				throw new CommitNotFoundException(refString, e);
			}
		}
		
		try (final RevWalk revWalk = new RevWalk(db)) {
			final Ref peeled = db.peel(ref);
		    return revWalk.parseCommit(peeled.getObjectId());
		} catch (final Exception e) {
			LOG.trace("This should never happen ;)", e);
			// Something horrible has probably happened, but we don't care really
		}
		return null;
	}
	
	public void addRefsToModel(final Model model, final Repository db) throws IOException {
		model.addAttribute("tags", db.getRefDatabase().getRefs(Constants.REFS_TAGS).keySet());
		model.addAttribute("branches", db.getRefDatabase().getRefs(Constants.REFS_HEADS).keySet());
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
