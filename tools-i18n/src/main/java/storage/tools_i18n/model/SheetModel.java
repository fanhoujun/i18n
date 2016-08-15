package storage.tools_i18n.model;

import java.util.List;

public class SheetModel {
	private final List<Message> modifiedMessages;
	private final List<Message> newMessages;
	private final List<Message> deletedMessages;
	private final List<Message> noChangeMessages;
	private final String sheetName;

	public String getSheetName() {
		return sheetName;
	}

	public List<Message> getModifiedMessages() {
		return modifiedMessages;
	}

	public List<Message> getNewMessages() {
		return newMessages;
	}

	public List<Message> getDeletedMessages() {
		return deletedMessages;
	}

	public List<Message> getNoChangeMessages() {
		return noChangeMessages;
	}

	public SheetModel(List<Message> modifiedMessages,
			List<Message> newMessages, List<Message> deletedMessages,
			List<Message> noChangeMessages, String sheetName) {
		this.modifiedMessages = modifiedMessages;
		this.newMessages = newMessages;
		this.deletedMessages = deletedMessages;
		this.noChangeMessages = noChangeMessages;
		this.sheetName = sheetName;
	}

}
