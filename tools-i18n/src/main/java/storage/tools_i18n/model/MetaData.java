package storage.tools_i18n.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class MetaData {
	
	public static final String META_EXPORT_ID="Export Id";
	public static final String META_APPLY_ID="Apply Id";
	public static final String META_CREATE_DATE="Created on";
	public static final String META_CREATE_BY="Created by"; 
	
	private String exportId;
	private String applyId;
	private String createDate;
	private String createdBy;
	private String workspaceCommitId;
	
	public String getWorkspaceCommitId() {
		return workspaceCommitId;
	}
	public void setWorkspaceCommitId(String workspaceCommitId) {
		this.workspaceCommitId = workspaceCommitId;
	}

	public String getExportId() {
		return exportId;
	}
	public void setExportId(String exportId) {
		this.exportId = exportId;
	}
	public String getApplyId() {
		return applyId;
	}
	public void setApplyId(String applyId) {
		this.applyId = applyId;
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
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put(META_CREATE_DATE, this.createDate);
		// update metadata.json field commit id with workspace commit id
		map.put(META_APPLY_ID, this.applyId);
		map.put(META_EXPORT_ID, this.exportId);
		map.put(META_CREATE_BY, this.createdBy);
		return map;
	}
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(META_CREATE_DATE+"="+this.createDate)
			.append(META_APPLY_ID+"="+this.applyId)
			.append(META_EXPORT_ID+"="+this.exportId)
			.append(META_CREATE_BY+"="+this.createdBy);
		return sb.toString();
	}
	
}
