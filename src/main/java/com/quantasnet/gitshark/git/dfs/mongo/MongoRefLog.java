package com.quantasnet.gitshark.git.dfs.mongo;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.quantasnet.gitshark.git.dfs.GitSharkDfsRefLog;

@Document
public class MongoRefLog implements GitSharkDfsRefLog {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private String id;

	@Indexed
	private String repoId;

	private String branch;
	
	private String oldId;
	private String newId;

	private String type;

	private DateTime time;
	private String userName;
	private String userEmail;
	private String userDisplayName;
	
	public String getId() {
		return id;
	}
	
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
	public String getBranch() {
		return branch;
	}
	
	@Override
	public void setBranch(String branch) {
		this.branch = branch;
	}
	
	@Override
	public String getOldId() {
		return oldId;
	}
	
	@Override
	public void setOldId(String oldId) {
		this.oldId = oldId;
	}
	
	@Override
	public String getNewId() {
		return newId;
	}
	
	@Override
	public void setNewId(String newId) {
		this.newId = newId;
	}
	
	@Override
	public DateTime getTime() {
		return time;
	}

	@Override
	public void setTime(DateTime time) {
		this.time = time;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	@Override
	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String getUserEmail() {
		return userEmail;
	}

	@Override
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	@Override
	public String getUserDisplayName() {
		return userDisplayName;
	}

	@Override
	public void setUserDisplayName(String userDisplayName) {
		this.userDisplayName = userDisplayName;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}
}