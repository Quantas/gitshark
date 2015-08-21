package com.quantasnet.gitshark.git.dfs.mongo;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.eclipse.jgit.lib.Ref.Storage;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.quantasnet.gitshark.git.dfs.GitSharkDfsRef;

@Document
public class MongoRef implements GitSharkDfsRef {

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

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getRepoId() {
		return repoId;
	}

	@Override
	public void setRepoId(String repoId) {
		this.repoId = repoId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Storage getStorage() {
		return storage;
	}

	@Override
	public void setStorage(Storage storage) {
		this.storage = storage;
	}

	@Override
	public String getObjectId() {
		return objectId;
	}

	@Override
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	@Override
	public boolean isSymbolic() {
		return symbolic;
	}
	
	@Override
	public void setSymbolic(boolean symbolic) {
		this.symbolic = symbolic;
	}
	
	@Override
	public String getTargetName() {
		return targetName;
	}
	
	@Override
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	
	@Override
	public String getTargetObjectId() {
		return targetObjectId;
	}
	
	@Override
	public void setTargetObjectId(String targetObjectId) {
		this.targetObjectId = targetObjectId;
	}
	
	@Override
	public boolean isPeeled() {
		return peeled;
	}

	@Override
	public void setPeeled(boolean peeled) {
		this.peeled = peeled;
	}
}
