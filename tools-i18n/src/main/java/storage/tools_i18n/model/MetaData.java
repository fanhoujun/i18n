package storage.tools_i18n.model;

import java.util.logging.Logger;

public class MetaData {
	private static Logger log = Logger.getLogger(MetaData.class.getName());
	
	public static final String META_LAST_TRANSLATED_COMMIT_ID="last.translated.commit.id";
	public static final String META_CURRENT_COMMIT_ID="current.commit.id";
	public static final String META_CREATE_DATE="Created on";
	public static final String META_CREATE_BY="Created by"; 
	
	private String lastTranslatedCommitId;
	private String currentCommitId;
	private String createDate;
	private String createdBy;
	public String getLastTranslatedCommitId() {
		if(lastTranslatedCommitId==null || "".equals(lastTranslatedCommitId.trim())){
			log.warning("Last Apply Translation CommitID not found, no previous codes will download!");
		}
		return lastTranslatedCommitId;
	}
	public void setLastTranslatedCommitId(String lastTranslatedCommitId) {
		this.lastTranslatedCommitId = lastTranslatedCommitId;
	}
	public String getCurrentCommitId() {
		return currentCommitId;
	}
	public void setCurrentCommitId(String currentCommitId) {
		this.currentCommitId = currentCommitId;
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
	
}
