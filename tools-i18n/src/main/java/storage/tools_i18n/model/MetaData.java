package storage.tools_i18n.model;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MetaData {
	private static Logger log = Logger.getLogger(MetaData.class.getName());
	
	public static final String META_COMMIT_ID="Commit Id";
	public static final String META_CREATE_DATE="Created on";
	public static final String META_CREATE_BY="Created by"; 
	
	private String commitId;
	private String createDate;
	private String createdBy;
	private String workspaceCommitId;
	
	public String getWorkspaceCommitId() {
		return workspaceCommitId;
	}
	public void setWorkspaceCommitId(String workspaceCommitId) {
		this.workspaceCommitId = workspaceCommitId;
	}
	public String getCommitId() {
		if(commitId==null || "".equals(commitId.trim())){
			log.warning("Last Apply Translation CommitID not found, no previous codes will download!");
		}
		return commitId;
	}
	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Map<String, String> converToMap(){
		Map<String, String> map = new HashMap<String, String>();
		map.put(META_CREATE_DATE, this.createDate);
		// update metadata.json field commit id with workspace commit id
		map.put(META_COMMIT_ID, this.getWorkspaceCommitId());
		map.put(META_CREATE_BY, this.createdBy);
		return map;
	}
	
}
