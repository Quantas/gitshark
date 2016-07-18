package com.quantasnet.gitshark.git.dfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.jgit.internal.storage.dfs.DfsObjDatabase;
import org.eclipse.jgit.internal.storage.dfs.DfsOutputStream;
import org.eclipse.jgit.internal.storage.dfs.DfsPackDescription;
import org.eclipse.jgit.internal.storage.dfs.DfsReaderOptions;
import org.eclipse.jgit.internal.storage.dfs.ReadableChannel;
import org.eclipse.jgit.internal.storage.pack.PackExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class GitSharkDfsObjDatabase extends DfsObjDatabase {

	private static final Logger LOG = LoggerFactory.getLogger(GitSharkDfsObjDatabase.class);

	private final GitSharkDfsRepository repository;
	private final GitSharkDfsService dfsService;
	
	GitSharkDfsObjDatabase(final GitSharkDfsRepository repository, final GitSharkDfsService dfsService) {
		super(repository, new DfsReaderOptions());
		this.repository = repository;
		this.dfsService = dfsService;
	}

	@Override
	protected DfsPackDescription newPack(final PackSource source) throws IOException {
		final DfsPackDescription desc = new DfsPackDescription(repository.getDescription(), UUID.randomUUID().toString());
		desc.setPackSource(source);
		return desc;
	}

	@Override
	protected void commitPackImpl(final Collection<DfsPackDescription> desc, final Collection<DfsPackDescription> replaces) throws IOException {
		if (null != replaces) {
			dfsService.deletePacks(replaces, repository.getId());
		}
	}

	@Override
	protected void rollbackPack(final Collection<DfsPackDescription> desc) {
		if (null != desc) {
			try {
				dfsService.deletePacks(desc, repository.getId());
			} catch (final Throwable t) {
				LOG.error("Error rolling back DFS packet!!", t);
			}
		}
	}

	@Override
	protected List<DfsPackDescription> listPacks() throws IOException {
		return dfsService.getPacks(repository.getId(), repository.getDescription());
	}

	@Override
	protected ReadableChannel openFile(final DfsPackDescription desc, final PackExt ext) throws FileNotFoundException, IOException {
		final byte[] data = dfsService.readFromPackFile(desc, ext, repository.getId());
		if (null != data) {
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
		throw new FileNotFoundException(desc.getFileName(ext));
	}

	@Override
	protected DfsOutputStream writeFile(final DfsPackDescription desc, final PackExt ext) throws IOException {
		final OutputStream outputStream = dfsService.getPackOutStream(desc, ext, repository.getId());
		return new DfsOutputStream() {
			
			@Override
			public void write(byte[] buf, int off, int len) throws IOException {
				outputStream.write(buf, off, len);
			}
			
			@Override
			public int read(long position, ByteBuffer buf) throws IOException {
				throw new NotImplementedException("NYI");
			}
			
			@Override
			public void close() throws IOException {
				outputStream.close();
			}
		};
	}
}