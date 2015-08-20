package com.quantasnet.gitserver.git.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
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
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.quantasnet.gitserver.git.cache.RepoCacheConstants;
import com.quantasnet.gitserver.git.exception.GitSharkErrorException;
import com.quantasnet.gitserver.git.exception.GitSharkException;
import com.quantasnet.gitserver.git.model.Commit;
import com.quantasnet.gitserver.git.model.Diff;
import com.quantasnet.gitserver.git.repo.GitRepository;

/**
 * Created by andrewlandsverk on 7/10/15.
 */
@Service
public class CommitService {

	private static final Logger LOG = LoggerFactory.getLogger(CommitService.class);

	@Autowired
	private RepositoryUtilities repoUtils;
	
	@Autowired
	private FilesystemRepositoryService repoService;

	@Cacheable(cacheNames = RepoCacheConstants.COMMIT_COUNT, key = "{ #repo.fullDisplayName }")
	public long commitCount(final GitRepository repo) throws GitSharkException {
		return repo.executeWithReturn(db -> {
			try {
				return StreamSupport.stream(Git.wrap(db).log().call().spliterator(), false).count();
			} catch (final GitAPIException e) {
				throw new GitSharkErrorException(e);
			}
		});
	}

	@Cacheable(cacheNames = RepoCacheConstants.ALL_COMMITS, key = "{ #repo.fullDisplayName, #selected }")
	public List<Commit> getCommits(final GitRepository repo, final String selected, final Repository db) throws IOException, GitSharkException {
		LOG.info("Cache Miss - {}, {}", repo.getFullDisplayName(), selected);

		final Map<ObjectId, String> branchHeads = new HashMap<>();

		RevCommit selectedCommit = null;

		if (null == selected) {
			final Set<String> branches = repoService.branches(repo).keySet();

			for (final String branch : branches) {
				try {
					final RevCommit commit = Git.wrap(db).log().add(db.resolve(branch)).setMaxCount(1).call().iterator().next();
					branchHeads.put(commit.getId(), branch);
				} catch (final GitAPIException e) {
					LOG.error("Error getting head commit for branch {}", branch, e);
				}
			}
		} else {
			selectedCommit = repoUtils.getRefHeadCommit(selected, repo, db);
			branchHeads.put(selectedCommit.getId(), selected);
		}

		final int maxCount = 50;
		final List<Commit> commits = new ArrayList<>();

		try (final RevWalk revWalk = new RevWalk(db)) {
			if (null == selectedCommit) {
				final List<RevCommit> headCommits = new ArrayList<>();
				final Map<String, Ref> refs = new HashMap<>();
				refs.putAll(repoService.branches(repo));
				refs.putAll(repoService.tags(repo));
				for (Ref ref : refs.values()) {
					if (!ref.isPeeled()) {
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
						LOG.trace("Exception while parsing what should have been a known objectid {}", objectId, e);
					}
					if (commit != null) {
						headCommits.add(commit);
					}
				}
				revWalk.markStart(headCommits);
			} else {
				revWalk.markStart(selectedCommit);
			}

			for (final RevCommit rev : revWalk) {
				commits.add(new Commit(rev, repo, branchHeads.get(rev.getId())));
				if (commits.size() == maxCount) {
					break;
				}
			}
		}

		return commits;
	}

	@Cacheable(cacheNames = RepoCacheConstants.COMMIT, key = "{ #repo.fullDisplayName, #parent == null ? '' : #parent.name, #commit.name }")
	public List<Diff> getDiffsForCommmit(final GitRepository repo, final Repository db, final RevCommit parent, final RevCommit commit, final RevWalk revWalk) throws IOException, GitAPIException {
		LOG.info("Cache Miss - {}, {}, {}", repo.getFullDisplayName(), parent == null ? "" : parent.getName(), commit.getName());

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

				final DiffEntry.ChangeType changeType = entry.getChangeType();

				diffs.add(new Diff(
					baos.toString(),
					changeType == DiffEntry.ChangeType.DELETE ? entry.getOldPath() : entry.getNewPath(),
					changeType)
				);
			}
		}

		return diffs;
	}

	private AbstractTreeIterator prepareTree(final RevCommit commit, final Repository db, final RevWalk revWalk) throws IOException {
		if (null == commit) {
			return new EmptyTreeIterator();
		} else {
			final RevTree tree = revWalk.parseTree(commit.getTree().getId());
			final CanonicalTreeParser parser = new CanonicalTreeParser();
			final ObjectReader reader = db.newObjectReader();

			parser.reset(reader, tree.getId());

			return parser;
		}
	}
}
