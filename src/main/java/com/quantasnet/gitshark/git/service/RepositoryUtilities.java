package com.quantasnet.gitshark.git.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jgit.api.Git;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.HandlerMapping;

import com.quantasnet.gitshark.git.exception.CommitNotFoundException;
import com.quantasnet.gitshark.git.exception.GitSharkErrorException;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.model.RepoFile;
import com.quantasnet.gitshark.git.repo.GitRepository;

@Service
public class RepositoryUtilities {

	private static final Logger LOG = LoggerFactory.getLogger(RepositoryUtilities.class);

	@Autowired
	private FilesystemRepositoryService repoService;
	
	public  String resolvePath(final HttpServletRequest req, final String repoPath, final String branch) {
		String path = ((String) req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).replaceFirst("\\/(raw|history)\\/", "/tree/");

		if (path.endsWith("/tree")) {
			path += "/" + branch + "/";
		}

		if (path.endsWith("/tree/")) {
			path += branch + "/";
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

	public boolean isPathRoot(final String path) {
		return "".equals(path);
	}

	@Cacheable(cacheNames = "hasCommits", key = "#repo.fullDisplayName")
	public boolean hasCommits(final GitRepository repo) throws GitSharkException {
		return repo.executeWithReturn(db -> {
			try {
				return Git.wrap(db).log().setMaxCount(1).call().iterator().hasNext();
			} catch (final GitAPIException e) {
				return false;
			}
		});
	}
	
	public List<RepoFile> getFiles(final GitRepository repo, final Repository db, final String branch, final String path) throws GitSharkException {
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
					if (directory) {
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
				}
			}
		} catch (final IOException | GitAPIException e) {
			throw new GitSharkErrorException(e);
		}
		
		collectFileContentsOrSortFiles(db, files);

		return files;
	}

	public RepoFile buildRepoFileObject(final GitRepository repo, final Repository db, final String path, final String ref, final boolean customPath, final String pathString,
			final boolean directory, final ObjectId objectId) throws GitAPIException, GitSharkException {
		
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
				walk.markStart(getRefHeadCommit(ref, repo, db));
				
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
	
	public RevCommit getRefHeadCommit(final String refString, final GitRepository repo, final Repository db) throws IOException, GitSharkException {
		final Ref branchRef = repoService.branches(repo).get(refString);
		final Ref tagRef = repoService.tags(repo).get(refString);
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
	
	public void addRefsToModel(final Model model, final GitRepository repo) throws GitSharkException {
		model.addAttribute("tags", repoService.tags(repo).keySet());
		model.addAttribute("branches", repoService.branches(repo).keySet());
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

	private void collectFileContentsOrSortFiles(final Repository db, final List<RepoFile> files) throws GitSharkErrorException {
		if (files.size() == 1) {
			// if we wanted just a single file, get it's contents for display and get out of here
			try {
				files.get(0).setFileContentsRaw(getFileContents(db, ObjectId.fromString(files.get(0).getObjectId())));
			} catch (IOException e) {
				throw new GitSharkErrorException(e);
			}
		} else if (files.size() > 1) {
			Collections.sort(files);
		}
	}
}
