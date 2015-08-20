package com.quantasnet.gitshark.git.dfs.mongo;

import java.util.List;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Ref.Storage;
import org.eclipse.jgit.lib.SymbolicRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFS;
import com.quantasnet.gitshark.git.exception.GitSharkErrorException;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.user.User;

/**
 * Created by andrewlandsverk on 8/17/15.
 */
@Service
public class MongoService {

	@Autowired
	private MongoRepoRepository mongoRepoRepository;
	
	@Autowired
	private MongoRefRepository mongoRefRepository;
	
	@Autowired
	private MongoDbFactory mongoDbFactory;
	
	public GridFS gridFS() {
		return new GridFS(mongoDbFactory.getDb());
	}
	
	public MongoRepo createRepo(final String name, final User user) throws GitSharkException {
		final MongoRepo repo = new MongoRepo();
		repo.setName(name);
		repo.setOwnerId(user.getId());
		return mongoRepoRepository.save(repo);
	}
	
	public boolean deleteRepo(final String name, final User user) {
		try {
			final MongoRepo repo = getRepo(name, user.getUserName(), user.getUserName(), user);
			gridFS().remove(new BasicDBObject("metadata.repoId", repo.getId()));
			mongoRepoRepository.delete(repo);
			mongoRefRepository.delete(mongoRefRepository.findByRepoId(repo.getId()));
			return true;
		} catch (final Exception e) {
			return false;
		}
	}
	
	public MongoRepo getRepo(final String name, final String owner, final String userName, final User user) throws GitSharkErrorException {
		if (owner.equals(userName)) {
			return mongoRepoRepository.findByOwnerIdAndName(user.getId(), name);
		}
		// TODO
		throw new GitSharkErrorException(new IllegalArgumentException("Not Yet Implemented."));
	}
	
	public List<MongoRepo> getAllReposForUser(final User user) {
		return mongoRepoRepository.findByOwnerId(user.getId());
	}
	
	
	public List<MongoRef> getAllRefsForRepo(final String repoId) {
		return mongoRefRepository.findByRepoId(repoId);
	}
	
	public MongoRef getRefByNameForRepo(final String name, final String repoId) {
		return mongoRefRepository.findByRepoIdAndName(repoId, name);
	}
	
	public boolean updateRefByNameForRepo(final String name, final String repoId, final Ref ref) {
		final MongoRef current = mongoRefRepository.findByRepoIdAndName(repoId, name);
		final MongoRef currentHEAD = mongoRefRepository.findByRepoIdAndName(repoId, "HEAD");
		if (current != null) {
			
			if (currentHEAD.getTargetName().equals(current.getName())) {
				// update HEAD
				currentHEAD.setObjectId(ref.getObjectId().name());
				currentHEAD.setTargetObjectId(ref.getObjectId().name());
				mongoRefRepository.save(currentHEAD);
			}
			
			final MongoRef newRef = buildFromRef(name, repoId, ref);
			newRef.setId(current.getId());
			mongoRefRepository.save(newRef);
			return true;
		}
		
		return false;
	}
	
	public boolean storeRefByNameForRepo(final String name, final String repoId, final Ref ref) {
		mongoRefRepository.save(buildFromRef(name, repoId, ref));
		return true;
	}
	
	public boolean deleteRefByNameForRepo(final String name, final String repoId) {
		final MongoRef existing = mongoRefRepository.findByRepoIdAndName(repoId, name);
		if (null == existing) {
			return false;
		}
		
		mongoRefRepository.delete(existing);
		return true;
	}
	
	private MongoRef buildFromRef(final String name, final String repoId, final Ref ref) {
		final MongoRef newRef = new MongoRef();
		newRef.setName(name);
		newRef.setObjectId(ref.getObjectId().name());
		newRef.setPeeled(ref.isPeeled());
		newRef.setRepoId(repoId);
		newRef.setStorage(Storage.LOOSE);
		newRef.setSymbolic(ref.isSymbolic());
		
		if (ref.isSymbolic()) {
			final Ref target = ((SymbolicRef)ref).getTarget();
			newRef.setTargetName(target.getName());
			newRef.setTargetObjectId(target.getObjectId().name());
		}
		
		return newRef;
	}
}