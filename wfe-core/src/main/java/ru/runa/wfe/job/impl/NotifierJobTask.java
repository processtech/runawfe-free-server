package ru.runa.wfe.job.impl;

public class NotifierJobTask extends JobTask<NotifierJobTaskExecutor> {

	@Override
	protected void execute() throws Exception {
		getTransactionalExecutor().executeInTransaction(false);
	}
	
}
