package com.quantasnet.gitshark.git.dfs.mongo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.internal.storage.dfs.DfsObjDatabase.PackSource;
import org.eclipse.jgit.internal.storage.dfs.DfsPackDescription;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.pack.PackExt;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Ref.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsRef;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsRefLog;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsRepo;
import com.quantasnet.gitshark.git.dfs.GitSharkDfsService;
import com.quantasnet.gitshark.git.dfs.GitSharkRepoSecurity;
import com.quantasnet.gitshark.git.exception.GitSharkException;
import com.quantasnet.gitshark.git.exception.RepositoryNotFoundException;
import com.quantasnet.gitshark.user.User;
import com.quantasnet.gitshark.user.UserService;

@Service
public class MongoDfsService implements GitSharkDfsService {

	private static final Logger LOG = LoggerFactory.getLogger(MongoDfsService.class);
	
	@Autowired
	private MongoRepoRepository mongoRepoRepository;
	
	@Autowired
	private MongoRefRepository mongoRefRepository;
	
	@Autowired
	private MongoRefLogRepository mongoRefLogRepository;
	
	@Autowired
	private MongoDbFactory mongoDbFactory;

	@Autowired
	private MongoRepoSecurityRepository mongoSecurityRepository;
	
	@Autowired
	private UserService userService;
	
	private GridFS gridFS() {
		return new GridFS(mongoDbFactory.getDb());
	}
	
	@Override
	public OutputStream getPackOutStream(final DfsPackDescription desc, final PackExt ext, final String repoId) {
		LOG.info("Creating new pack file = {}, {}", desc, ext.getExtension());
		final GridFSInputFile newFile = gridFS().createFile();
		newFile.setMetaData(createMetadata(desc, ext, repoId));
		return newFile.getOutputStream();
	}
	
	@Override
	public List<DfsPackDescription> getPacks(final String repoId, final DfsRepositoryDescription desc) {
		LOG.debug("Reading all packs for {}", desc.getRepositoryName());
		final List<GridFSDBFile> dbFiles = gridFS().find(new BasicDBObject("metadata.repoId", repoId));
		return dbFiles
			.stream()
			.map(file -> {
				final DBObject metadata = file.getMetaData();
				final String fileName = (String) metadata.get("fileName");
				return createDescriptionFromMetadata(metadata, fileName, desc);
			})
			.collect(Collectors.toList());
	}
	
	@Override
	public byte[] readFromPackFile(final DfsPackDescription desc, final PackExt ext, final String repoId) {
		LOG.info("Reading from pack = {}, {}", desc, ext.getExtension());
		final GridFSDBFile file = gridFS().findOne(new BasicDBObject("metadata.fileName", desc.getFileName(ext)).append("metadata.repoId", repoId));
		if (null != file) {
			try (final InputStream inputStream = file.getInputStream()) {
				return IOUtils.toByteArray(inputStream);
			} catch (final IOException e) {
				LOG.error("Error closing GridFS inputStream", e);
			}
		}
		return null;
	}
	
	@Override
	public void deletePacks(final Collection<DfsPackDescription> replaces, final String repoId) {
		replaces
				.stream()
				.map(pack -> pack.getFileName(PackExt.PACK))
				.collect(Collectors.toSet())

				.forEach(pack -> {
					final String packNameNoExt = pack.substring(0, pack.lastIndexOf('.'));
					LOG.info("Deleting Pack = {}", packNameNoExt);

					final BasicDBObject query = new BasicDBObject();
					query.put("metadata.fileName", Pattern.compile(packNameNoExt + "\\.(idx|pack|bitmap)"));
					query.put("metadata.repoId", repoId);

					gridFS().remove(query);
				});
	}
	
	@Override
	public long repositorySize(final String repoId) {
		return gridFS().find(new BasicDBObject("metadata.repoId", repoId)).stream().mapToLong(GridFSFile::getLength).sum();
	}
	
	@Override
	public GitSharkDfsRepo createRepo(final String name, final User user) throws GitSharkException {
		final MongoRepo repo = new MongoRepo();
		repo.setName(name);
		repo.setOwnerId(user.getId());
		return mongoRepoRepository.save(repo);
	}
	
	@Override
	public boolean deleteRepo(final String name, final User user) {
		try {
			final MongoRepo repo = getRepo(name, user.getUserName());
			gridFS().remove(new BasicDBObject("metadata.repoId", repo.getId()));
			mongoRepoRepository.delete(repo);
			mongoRefRepository.delete(mongoRefRepository.findByRepoId(repo.getId()));
			return true;
		} catch (final Exception e) {
			LOG.error("Fatal error deleting repo", e);
			return false;
		}
	}
	
	@Override
	public MongoRepo getRepo(final String name, final String owner) throws RepositoryNotFoundException {
		final User repoOwner = userService.getUserByUsername(owner);
		if (null != repoOwner) {
			final MongoRepo repo = mongoRepoRepository.findByOwnerIdAndName(repoOwner.getId(), name);
			if (null != repo) {
				return repo;
			}
		}
		
		throw new RepositoryNotFoundException(name);
	}
	
	@Override
	public List<? extends GitSharkDfsRepo> getAllReposForUser(final User user) {
		return mongoRepoRepository.findByOwnerId(user.getId());
	}
	
	@Override
	public List<? extends GitSharkDfsRef> getAllRefsForRepo(final String repoId) {
		return mongoRefRepository.findByRepoId(repoId);
	}

	@Override
	public GitSharkRepoSecurity getSecurityForRepo(final String repoId) {
		final GitSharkRepoSecurity security = mongoSecurityRepository.findByRepoId(repoId);
		return null != security ? security : new MongoRepoSecurity();
	}

	@Override
	public void saveSecurityForRepo(final GitSharkRepoSecurity security) {
		if (security instanceof MongoRepoSecurity) {
			mongoSecurityRepository.save((MongoRepoSecurity) security);
		}
	}

	@Override
	public GitSharkDfsRef getRefByNameForRepo(final String name, final String repoId) {
		return mongoRefRepository.findByRepoIdAndName(repoId, name);
	}
	
	@Override
	public boolean updateRefByNameForRepo(final String name, final String repoId, final Ref ref) {
		final GitSharkDfsRef current = mongoRefRepository.findByRepoIdAndName(repoId, name);
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
	
	@Override
	public boolean storeRefByNameForRepo(final String name, final String repoId, final Ref ref) {
		mongoRefRepository.save(buildFromRef(name, repoId, ref));
		return true;
	}
	
	@Override
	public boolean deleteRefByNameForRepo(final String name, final String repoId) {
		final MongoRef existing = mongoRefRepository.findByRepoIdAndName(repoId, name);
		if (null == existing) {
			return false;
		}
		
		mongoRefRepository.delete(existing);
		return true;
	}
	
	@Override
	public GitSharkDfsRefLog createEmptyRefLog() {
		return new MongoRefLog();
	}
	
	@Override
	public void saveRefLog(final GitSharkDfsRefLog refLog) {
		if (refLog instanceof MongoRefLog) {
			mongoRefLogRepository.save((MongoRefLog) refLog);
		}
	}
	
	@Override
	public List<? extends GitSharkDfsRefLog> getRefLogForRepo(final String repoId) {
		return mongoRefLogRepository.findByRepoId(repoId);
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
			final Ref target = ref.getTarget();
			newRef.setTargetName(target.getName());
			newRef.setTargetObjectId(target.getObjectId().name());
		}
		
		return newRef;
	}
	
	private BasicDBObject createMetadata(final DfsPackDescription desc, final PackExt ext, final String repoId) {
		final MongoObjMetadata metadata = new MongoObjMetadata();
		metadata.setDeltaCount(desc.getDeltaCount());
		metadata.setFileName(desc.getFileName(ext));
		metadata.setExtension(ext.getExtension());
		metadata.setIndexVersion(desc.getIndexVersion());
		metadata.setLastModified(desc.getLastModified());
		metadata.setObjectCount(desc.getObjectCount());
		metadata.setPackSource(desc.getPackSource());
		metadata.setRepoId(repoId);
		return metadata.build();
	}
	
	private DfsPackDescription createDescriptionFromMetadata(final DBObject dbObject, final String fileName, final DfsRepositoryDescription description) {
		final DfsPackDescription dfsPackDescription = new DfsPackDescription(description, fileName);
		final PackExt ext = buildExt(dbObject.get("extension"));
		if (null != ext) {
			dfsPackDescription.addFileExt(ext);
		}
		dfsPackDescription.setLastModified((long) dbObject.get("lastModified"));
		dfsPackDescription.setObjectCount((long) dbObject.get("objectCount"));
		dfsPackDescription.setDeltaCount((long) dbObject.get("deltaCount"));
		dfsPackDescription.setIndexVersion((int) dbObject.get("indexVersion"));
		dfsPackDescription.setPackSource(PackSource.valueOf((String) dbObject.get("packSource")));
		return dfsPackDescription;
	}

	private PackExt buildExt(final Object extension) {
		if (PackExt.INDEX.getExtension().equals(extension)) {
			return PackExt.INDEX;
		} else if (PackExt.PACK.getExtension().equals(extension)) {
			return PackExt.PACK;
		} else if (PackExt.BITMAP_INDEX.getExtension().equals(extension)) {
			return PackExt.BITMAP_INDEX;
		}
		return null;
	}
}