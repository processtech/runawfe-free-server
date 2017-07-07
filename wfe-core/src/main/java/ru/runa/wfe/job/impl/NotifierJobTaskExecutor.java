package ru.runa.wfe.job.impl;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.extension.handler.ExpiredTaskNotifierHandler;

public class NotifierJobTaskExecutor extends TransactionalExecutor {

	@Autowired
	private ExpiredTaskNotifierHandler expiredTaskNotifierHandler;
	
	@Override
	protected void doExecuteInTransaction() throws Exception {
		expiredTaskNotifierHandler.execute();
	}

}
