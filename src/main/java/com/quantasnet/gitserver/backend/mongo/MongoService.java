package com.quantasnet.gitserver.backend.mongo;

import java.util.List;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.SymbolicRef;
import org.eclipse.jgit.lib.Ref.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.stereotype.Service;

import com.mongodb.gridfs.GridFS;
import com.quantasnet.gitserver.git.exception.GitServerException;
import com.quantasnet.gitserver.user.User;

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
	
	public MongoRepo createRepo(final String name, final User user) throws GitServerException {
		final MongoRepo repo = new MongoRepo();
		repo.setName(name);
		repo.setOwnerId(user.getId());
		return mongoRepoRepository.save(repo);
	}
	
	public MongoRepo getRepo(final String name, final User user) {
		return mongoRepoRepository.findByOwnerIdAndName(user.getId(), name);
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