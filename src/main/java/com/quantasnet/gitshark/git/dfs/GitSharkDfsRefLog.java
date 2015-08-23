package com.quantasnet.gitshark.git.dfs;

import org.joda.time.DateTime;

public interface GitSharkDfsRefLog {

	String getRepoId();
	void setRepoId(String repoId);
	
	String getBranch();
	void setBranch(String branch);
	
	String getOldId();
	void setOldId(String oldId);
	
	String getNewId();
	void setNewId(String newId);
	
	DateTime getTime();
	void setTime(DateTime time);
	
	String getUserName();
	void setUserName(String userName);
	
	String getUserEmail();
	void setUserEmail(String userEmail);
	
	String getUserDisplayName();
	void setUserDisplayName(String userDisplayName);

	String getType();
	void setType(String type);
}