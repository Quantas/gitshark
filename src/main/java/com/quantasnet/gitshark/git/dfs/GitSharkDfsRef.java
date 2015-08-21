package com.quantasnet.gitshark.git.dfs;

import org.eclipse.jgit.lib.Ref.Storage;

public interface GitSharkDfsRef {

	String getId();

	void setId(String id);

	String getRepoId();

	void setRepoId(String repoId);

	String getName();

	void setName(String name);

	Storage getStorage();

	void setStorage(Storage storage);

	String getObjectId();

	void setObjectId(String objectId);

	boolean isSymbolic();

	void setSymbolic(boolean symbolic);

	String getTargetName();

	void setTargetName(String targetName);

	String getTargetObjectId();

	void setTargetObjectId(String targetObjectId);

	boolean isPeeled();

	void setPeeled(boolean peeled);

}