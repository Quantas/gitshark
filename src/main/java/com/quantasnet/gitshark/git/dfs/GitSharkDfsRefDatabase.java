package com.quantasnet.gitshark.git.dfs;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.eclipse.jgit.internal.storage.dfs.DfsRefDatabase;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectIdRef;
import org.eclipse.jgit.lib.ObjectIdRef.Unpeeled;
import org.eclipse.jgit.lib.Ref.Storage;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.SymbolicRef;
import org.eclipse.jgit.util.RefList;

import com.quantasnet.gitshark.git.dfs.mongo.MongoRef;

public class GitSharkDfsRefDatabase extends DfsRefDatabase {

	private final GitSharkDfsService dfsService;
	private final GitSharkDfsRepository repository;
	
	GitSharkDfsRefDatabase(final GitSharkDfsRepository repository, final GitSharkDfsService dfsService) {
		super(repository);
		this.repository = repository;
		this.dfsService = dfsService;
	}

	@Override
	protected RefCache scanAllRefs() throws IOException {
		final List<MongoRef> mongoRefs = dfsService.getAllRefsForRepo(repository.getId());
		final RefList.Builder<Ref> ids = new RefList.Builder<Ref>();
		final RefList.Builder<Ref> sym = new RefList.Builder<Ref>();
		
		for (final GitSharkDfsRef ref : mongoRefs) {
			if (ref.isSymbolic()) {
				final Ref newRef = new SymbolicRef(ref.getName(), new Unpeeled(ref.getStorage(), ref.getTargetName(), ObjectId.fromString(ref.getTargetObjectId())));
				sym.add(newRef);
				ids.add(newRef);
			} else {
				ids.add(new Unpeeled(ref.getStorage(), ref.getName(), ObjectId.fromString(ref.getObjectId())));
			}
		}
		
		if (sym.size() == 0 && ids.size() > 0) {
			// HEAD is missing
			for (final Ref ref : ids.toRefList()) {
				if ("refs/heads/master".equals(ref.getName())) {
					final Ref head = new SymbolicRef("HEAD", new Unpeeled(ref.getStorage(), ref.getName(), ref.getObjectId()));
					sym.add(head);
					ids.add(head);
					dfsService.storeRefByNameForRepo("HEAD", repository.getId(), head);
					break;
				}
			}
		}
		
		ids.sort();
		sym.sort();
		return new RefCache(ids.toRefList(), sym.toRefList());
	}

	@Override
	protected boolean compareAndPut(final Ref oldRef, final Ref newRef) throws IOException {
		final ObjectId id = newRef.getObjectId();
		
		if (id != null) {
			try (RevWalk rw = new RevWalk(getRepository())) {
				// Validate that the target exists in a new RevWalk, as the RevWalk
				// from the RefUpdate might be reading back unflushed objects.
				rw.parseAny(id);
			}
		}
		
		String name = newRef.getName();
		if (oldRef == null) {
			return dfsService.storeRefByNameForRepo(name, repository.getId(), newRef);
		}

		final GitSharkDfsRef cur = dfsService.getRefByNameForRepo(name, repository.getId());
		if (cur != null) {
			Ref toCompare = new Unpeeled(cur.getStorage(), cur.getName(), ObjectId.fromString(cur.getObjectId()));
			
			if (cur.isSymbolic()) {
					toCompare = new SymbolicRef(name, new ObjectIdRef.Unpeeled(Storage.NEW, toCompare.getName(), toCompare.getObjectId()));
			} else {
				toCompare = toCompare.getLeaf();
			}
			
			if (eq(toCompare, oldRef)) {
				return dfsService.updateRefByNameForRepo(name, repository.getId(), newRef);
			}
		}

		if (oldRef.getStorage() == Storage.NEW) {
			final boolean created = dfsService.storeRefByNameForRepo(name, repository.getId(), newRef);
			if (created) {
				// Make sure HEAD exists
				scanAllRefs();
			}
			return created;
		}

		return false;
	}

	@Override
	protected boolean compareAndRemove(final Ref oldRef) throws IOException {
		final String name = oldRef.getName();
		
		final GitSharkDfsRef cur = dfsService.getRefByNameForRepo(name, repository.getId());
		if (cur != null) {
			final Ref toCompare = new Unpeeled(cur.getStorage(), cur.getName(), ObjectId.fromString(cur.getObjectId()));
			
			if (eq(toCompare, oldRef)) {
				return dfsService.deleteRefByNameForRepo(name, repository.getId());
			}
		}
		
		return false;
	}
	
	private boolean eq(Ref a, Ref b) {
		if (!Objects.equals(a.getName(), b.getName())) {
			return false;
		}
		return Objects.equals(a.getLeaf().getObjectId(), b.getLeaf().getObjectId());
	}

}
