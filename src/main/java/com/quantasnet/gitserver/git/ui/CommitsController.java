package com.quantasnet.gitserver.git.ui;

import static org.eclipse.jgit.lib.RefDatabase.ALL;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.quantasnet.gitserver.git.model.Commit;
import com.quantasnet.gitserver.git.model.Diff;
import com.quantasnet.gitserver.git.repo.GitRepository;

/**
 * TODO parse range with ([-+]\d++),(\d++)\s([+-]\d++),(\d++)
 * @author andrewlandsverk
 *
 */
@RequestMapping("/repo/{repoOwner}/{repoName}")
@Controller
public class CommitsController {

	@RequestMapping("/commits")
	public String showLog(final GitRepository repo, @PathVariable final String repoOwner, @PathVariable final String repoName, final Model model) throws Exception {
		repo.execute(db -> {
			if (repo.hasCommits()) {
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
						commits.add(new Commit(rev, repo));
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
	
	@RequestMapping(value = "/commit/{commitId}")
	public String singleCommit(final GitRepository repo, @PathVariable final String commitId, final Model model) throws Exception {
		if (repo.hasCommits()) {
			repo.execute(db -> {
				final RevWalk revWalk = new RevWalk(db);
				final RevCommit commit = revWalk.parseCommit(ObjectId.fromString(commitId));
				final RevCommit parent = revWalk.parseCommit(commit.getParent(0).getId());
				
				final List<DiffEntry> diff = Git.wrap(db)
						.diff()
						.setOldTree(prepareTree(parent, db, revWalk))
						.setNewTree(prepareTree(commit, db, revWalk))
						.call();
				
				final List<Diff> diffs = new ArrayList<>();
				
				for (final DiffEntry entry : diff) {
		            try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		            		final DiffFormatter formatter = new DiffFormatter(baos)) {
	            		formatter.setRepository(db);
			            formatter.format(entry);
			            
			            final ChangeType changeType = entry.getChangeType();
			            
			            diffs.add(
		            		new Diff(
	            				baos.toString(), 
	            				changeType == ChangeType.DELETE ? entry.getOldPath() : entry.getNewPath(), 
	    						changeType)
		            		);
	            	}
	            }
	            
	            model.addAttribute("diffs", diffs);
	            model.addAttribute("commit", new Commit(commit, repo));
			});
		}
		
		return "git/commit";
	}
	
	private AbstractTreeIterator prepareTree(final RevCommit commit, final Repository db, final RevWalk revWalk) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		final RevTree tree = revWalk.parseTree(commit.getTree().getId());
		final CanonicalTreeParser parser = new CanonicalTreeParser();
        final ObjectReader reader = db.newObjectReader();
        
        parser.reset(reader, tree.getId());
        
        return parser;
	}
}
