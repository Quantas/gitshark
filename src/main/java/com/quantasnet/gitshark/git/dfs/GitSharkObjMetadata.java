package com.quantasnet.gitshark.git.dfs;

import org.eclipse.jgit.internal.storage.dfs.DfsObjDatabase.PackSource;

import com.mongodb.BasicDBObject;

class GitSharkObjMetadata {

	private String repoId;
	private String fileName;
	private String packSource;
	private Long lastModified;
	private Long objectCount;
	private Long deltaCount;
	private Integer indexVersion;
	
	public BasicDBObject build() {
		final BasicDBObject returnObj = new BasicDBObject();
		returnObj.put("repoId", repoId);
		addIfExists(returnObj, "fileName", fileName);
		addIfExists(returnObj, "packSource", packSource);
		addIfExists(returnObj, "lastModified", lastModified);
		addIfExists(returnObj, "objectCount", objectCount);
		addIfExists(returnObj, "deltaCount", deltaCount);
		addIfExists(returnObj, "indexVersion", indexVersion);
		return returnObj;
	}

	private void addIfExists(final BasicDBObject obj, final String name, final Object value) {
		if (null != value) {
			obj.put(name, value);
		}
	}
	
	public String getRepoId() {
		return repoId;
	}
	
	public void setRepoId(String repoId) {
		this.repoId = repoId;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public PackSource getPackSource() {
		return PackSource.valueOf(packSource);
	}
	
	public void setPackSource(PackSource packSource) {
		this.packSource = packSource.name();
	}
	
	public long getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
	
	public long getObjectCount() {
		return objectCount;
	}
	
	public void setObjectCount(long objectCount) {
		this.objectCount = objectCount;
	}
	
	public long getDeltaCount() {
		return deltaCount;
	}
	
	public void setDeltaCount(long deltaCount) {
		this.deltaCount = deltaCount;
	}
	
	public int getIndexVersion() {
		return indexVersion;
	}
	
	public void setIndexVersion(int indexVersion) {
		this.indexVersion = indexVersion;
	}
}
