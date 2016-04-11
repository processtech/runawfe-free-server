package ru.runa.wfe.service.impl;

import ru.runa.wfe.InternalApplicationException;

/**
 * Means that there is no 'ReceiveMessage' node handler yet, send JMS message
 * back to the queue.
 * 
 * @author dofs
 * @since 4.0
 */
public class MessagePostponedException extends InternalApplicationException {
	private static final long serialVersionUID = 1L;

	public MessagePostponedException(String message) {
		super(message);
	}

}
