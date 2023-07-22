package ru.runa.wfe.definition;

import ru.runa.wfe.InternalApplicationException;

public class BotTaskFileDoesNotExistException extends InternalApplicationException {
	private static final long serialVersionUID = 1L;

	public BotTaskFileDoesNotExistException(String fileName) {
		super(fileName);
	}
}
