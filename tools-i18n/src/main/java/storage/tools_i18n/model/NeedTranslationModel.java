package storage.tools_i18n.model;

import java.util.List;

public class NeedTranslationModel {
	List<Message> modifiedMessages;
	List<Message> newMessages;
	List<Message> deletedMessages; 
	List<Message> noChangeMessages;
	
	private String sheetName;
	
	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public List<Message> getModifiedMessages() {
		return modifiedMessages;
	}

	public void setModifiedMessages(List<Message> modifiedMessages) {
		this.modifiedMessages = modifiedMessages;
	}

	public List<Message> getNewMessages() {
		return newMessages;
	}

	public void setNewMessages(List<Message> newMessages) {
		this.newMessages = newMessages;
	}

	public List<Message> getDeletedMessages() {
		return deletedMessages;
	}

	public void setDeletedMessages(List<Message> deletedMessages) {
		this.deletedMessages = deletedMessages;
	}

	public List<Message> getNoChangeMessages() {
		return noChangeMessages;
	}

	public void setNoChangeMessages(List<Message> noChangeMessages) {
		this.noChangeMessages = noChangeMessages;
	}


	public NeedTranslationModel(List<Message> modifiedMessages,
			List<Message> newMessages, List<Message> deletedMessages,
			List<Message> noChangeMessages) {
		super();
		this.modifiedMessages = modifiedMessages;
		this.newMessages = newMessages;
		this.deletedMessages = deletedMessages;
		this.noChangeMessages = noChangeMessages;
	}
	
}
