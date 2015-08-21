package com.quantasnet.gitshark.git.dfs.mongo;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.quantasnet.gitshark.git.dfs.GitSharkDfsRepo;

@Document
public class MongoRepo implements GitSharkDfsRepo {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private String id;
	
	@Indexed
	private String ownerId;
	
	private String name;
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public String getOwnerId() {
		return ownerId;
	}
	
	@Override
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
}