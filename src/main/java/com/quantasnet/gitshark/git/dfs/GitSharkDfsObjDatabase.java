package com.quantasnet.gitshark.git.dfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.jgit.internal.storage.dfs.DfsObjDatabase;
import org.eclipse.jgit.internal.storage.dfs.DfsOutputStream;
import org.eclipse.jgit.internal.storage.dfs.DfsPackDescription;
import org.eclipse.jgit.internal.storage.dfs.DfsReaderOptions;
import org.eclipse.jgit.internal.storage.dfs.ReadableChannel;
import org.eclipse.jgit.internal.storage.pack.PackExt;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.quantasnet.gitshark.git.dfs.mongo.MongoService;

public class GitSharkDfsObjDatabase extends DfsObjDatabase {
	
	private GitSharkDfsRepository repository;
	private final MongoService mongoService;
	
	GitSharkDfsObjDatabase(final GitSharkDfsRepository repository, final MongoService mongoService) {
		super(repository, new DfsReaderOptions());
		this.repository = repository;
		this.mongoService = mongoService;
	}

	@Override
	protected DfsPackDescription newPack(final PackSource source) throws IOException {
		final DfsPackDescription desc = new DfsPackDescription(repository.getDescription(), UUID.randomUUID().toString());
		desc.setPackSource(source);
		return desc;
	}

	@Override
	protected void commitPackImpl(Collection<DfsPackDescription> desc, Collection<DfsPackDescription> replaces) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	protected void rollbackPack(Collection<DfsPackDescription> desc) {
		// TODO Auto-generated method stub
	}

	@Override
	protected List<DfsPackDescription> listPacks() throws IOException {
		final List<GridFSDBFile> dbFiles = mongoService.gridFS().find(new BasicDBObject("metadata.repoId", repository.getId()));
		final List<DfsPackDescription> packs = new ArrayList<>();
		for (final GridFSDBFile file : dbFiles) {
			final DBObject metadata = file.getMetaData();
			final String fileName = (String) metadata.get("fileName");
			//if (fileName.endsWith(".pack")) {
				packs.add(createDescriptionFromMetadata(metadata, fileName));
			//}
		}
		return packs;
	}

	@Override
	protected ReadableChannel openFile(DfsPackDescription desc, PackExt ext) throws FileNotFoundException, IOException {
		final GridFSDBFile file = mongoService.gridFS().findOne(new BasicDBObject("metadata.fileName", desc.getFileName(ext)).append("metadata.repoId", repository.getId()));
		if (null != file) {
			final InputStream inputStream = file.getInputStream();
			final byte[] data = IOUtils.toByteArray(inputStream);
			IOUtils.closeQuietly(inputStream);
			
			return new ReadableChannel() {
				
				private boolean open = true;
				private int position;
				
				@Override
				public boolean isOpen() {
					return open;
				}
				
				@Override
				public void close() throws IOException {
					open = false;
				}
				
				@Override
				public int read(ByteBuffer dst) {
					int n = Math.min(dst.remaining(), data.length - position);
					if (n == 0) {
						return -1;
					}
					dst.put(data, position, n);
					position += n;
					return n;
				}
				
				@Override
				public long size() throws IOException {
					return data.length;
				}
				
				@Override
				public void setReadAheadBytes(int bufferSize) throws IOException {
					// no-op
				}
				
				@Override
				public void position(long newPosition) throws IOException {
					position = (int) newPosition;
				}
				
				@Override
				public long position() throws IOException {
					return position;
				}
				
				@Override
				public int blockSize() {
					return 0;
				}
			};
		}
		
		
		throw new IllegalArgumentException();
	}

	@Override
	protected DfsOutputStream writeFile(final DfsPackDescription desc, final PackExt ext) throws IOException {
		final GridFSInputFile newFile = mongoService.gridFS().createFile();
		newFile.setMetaData(createMetadata(desc, ext));
		return new DfsOutputStream() {
			
			@Override
			public void write(byte[] buf, int off, int len) throws IOException {
				newFile.getOutputStream().write(buf, off, len);
			}
			
			@Override
			public int read(long position, ByteBuffer buf) throws IOException {
				throw new NotImplementedException("NYI");
			}
			
			@Override
			public void close() throws IOException {
				newFile.getOutputStream().close();
			}
		};
	}
	
	private BasicDBObject createMetadata(final DfsPackDescription desc, final PackExt ext) {
		final GitSharkObjMetadata metadata = new GitSharkObjMetadata();
		metadata.setDeltaCount(desc.getDeltaCount());
		metadata.setFileName(desc.getFileName(ext));
		metadata.setIndexVersion(desc.getIndexVersion());
		metadata.setLastModified(desc.getLastModified());
		metadata.setObjectCount(desc.getObjectCount());
		metadata.setPackSource(desc.getPackSource());
		metadata.setRepoId(repository.getId());
		return metadata.build();
	}
	
	private DfsPackDescription createDescriptionFromMetadata(final DBObject dbObject, final String fileName) {
		final DfsPackDescription dfsPackDescription = new DfsPackDescription(repository.getDescription(), fileName);
		dfsPackDescription.setLastModified((long) dbObject.get("lastModified"));
		dfsPackDescription.setObjectCount((long) dbObject.get("objectCount"));
		dfsPackDescription.setDeltaCount((long) dbObject.get("deltaCount"));
		dfsPackDescription.setIndexVersion((int) dbObject.get("indexVersion"));
		dfsPackDescription.setPackSource(PackSource.valueOf((String) dbObject.get("packSource")));
		return dfsPackDescription;
	}
}