package ru.runa.wfe.job.impl;

public class ImportAggregatedHistoryTask extends JobTask<AggregatedHistoryImporter> {

    @Override
    protected void execute() throws Exception {
        getTransactionalExecutor().executeInTransaction(false);
    }
}
