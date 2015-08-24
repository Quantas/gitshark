package com.quantasnet.gitshark.git.dfs.mongo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.quantasnet.gitshark.git.dfs.GitSharkRepoSecurity;

@Document
public class MongoRepoSecurity implements GitSharkRepoSecurity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private String id;
	@Indexed(unique = true)
	private String repoId;
	private boolean anonRead;
	private boolean anonWrite;
	private List<String> users = new ArrayList<>();

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
	public boolean isAnonRead() {
		return anonRead;
	}

	@Override
	public void setAnonRead(boolean anonRead) {
		this.anonRead = anonRead;
	}

	@Override
	public boolean isAnonWrite() {
		return anonWrite;
	}

	@Override
	public void setAnonWrite(boolean anonWrite) {
		this.anonWrite = anonWrite;
	}

	@Override
	public List<String> getUsers() {
		return users;
	}

	@Override
	public void setUsers(List<String> users) {
		this.users = users;
	}
}
