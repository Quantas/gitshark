package com.quantasnet.gitserver.git.backend.mongo;

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

import com.quantasnet.gitserver.backend.mongo.MongoRef;
import com.quantasnet.gitserver.backend.mongo.MongoService;

public class MongoDfsRefDatabase extends DfsRefDatabase {

	private MongoService mongoService;
	private MongoDfsRepository mongoRepo;
	
	MongoDfsRefDatabase(final MongoDfsRepository repository, final MongoService mongoService) {
		super(repository);
		this.mongoRepo = repository;
		this.mongoService = mongoService;
	}

	@Override
	protected RefCache scanAllRefs() throws IOException {
		final List<MongoRef> mongoRefs = mongoService.getAllRefsForRepo(mongoRepo.getId());
		final RefList.Builder<Ref> ids = new RefList.Builder<Ref>();
		final RefList.Builder<Ref> sym = new RefList.Builder<Ref>();
		
		for (final MongoRef ref : mongoRefs) {
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
				if (ref.getName().equals("refs/heads/master")) {
					final Ref head = new SymbolicRef("HEAD", new Unpeeled(ref.getStorage(), ref.getName(), ref.getObjectId()));
					sym.add(head);
					ids.add(head);
					mongoService.storeRefByNameForRepo("HEAD", mongoRepo.getId(), head);
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
			return mongoService.storeRefByNameForRepo(name, mongoRepo.getId(), newRef);
		}

		final MongoRef cur = mongoService.getRefByNameForRepo(name, mongoRepo.getId());
		if (cur != null) {
			Ref toCompare = new Unpeeled(cur.getStorage(), cur.getName(), ObjectId.fromString(cur.getObjectId()));
			
			if (toCompare.isSymbolic()) {
					toCompare = new SymbolicRef(name, new ObjectIdRef.Unpeeled(Storage.NEW, toCompare.getName(), toCompare.getObjectId()));
			} else {
				toCompare = toCompare.getLeaf();
			}
			
			if (eq(toCompare, oldRef)) {
				return mongoService.storeRefByNameForRepo(name, mongoRepo.getId(), newRef);
			}
		}

		if (oldRef.getStorage() == Storage.NEW) {
			final boolean created = mongoService.storeRefByNameForRepo(name, mongoRepo.getId(), newRef);
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
		
		final MongoRef cur = mongoService.getRefByNameForRepo(name, mongoRepo.getId());
		if (cur != null) {
			final Ref toCompare = new Unpeeled(cur.getStorage(), cur.getName(), ObjectId.fromString(cur.getObjectId()));
			
			if (eq(toCompare, oldRef)) {
				return mongoService.deleteRefByNameForRepo(name, mongoRepo.getId());
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
