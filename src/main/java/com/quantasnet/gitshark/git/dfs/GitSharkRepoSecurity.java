package com.quantasnet.gitshark.git.dfs;

import java.util.List;

public interface GitSharkRepoSecurity {

	String getId();
	void setId(String id);

	String getRepoId();
	void setRepoId(String repoId);

	boolean isAnonRead();
	void setAnonRead(boolean anonRead);

	boolean isAnonWrite();
	void setAnonWrite(boolean anonWrite);

	List<String> getUsers();
	void setUsers(List<String> users);

}
