package com.quantasnet.gitshark.git.dfs.mongo;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.eclipse.jgit.lib.Ref.Storage;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class MongoRef {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private String id;
	@Indexed
	private String repoId;
	private String name;
	private Storage storage;
	private String objectId;
	private boolean symbolic;
	private String targetObjectId;
	private String targetName;
	private boolean peeled;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRepoId() {
		return repoId;
	}

	public void setRepoId(String repoId) {
		this.repoId = repoId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Storage getStorage() {
		return storage;
	}

	public void setStorage(Storage storage) {
		this.storage = storage;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public boolean isSymbolic() {
		return symbolic;
	}
	
	public void setSymbolic(boolean symbolic) {
		this.symbolic = symbolic;
	}
	
	public String getTargetName() {
		return targetName;
	}
	
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	
	public String getTargetObjectId() {
		return targetObjectId;
	}
	
	public void setTargetObjectId(String targetObjectId) {
		this.targetObjectId = targetObjectId;
	}
	
	public boolean isPeeled() {
		return peeled;
	}

	public void setPeeled(boolean peeled) {
		this.peeled = peeled;
	}
}
