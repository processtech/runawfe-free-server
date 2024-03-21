package ru.runa.wfe.execution.dao;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.CommonDao;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.QCurrentNodeProcess;
import ru.runa.wfe.execution.QCurrentProcess;
import ru.runa.wfe.execution.QCurrentToken;
import ru.runa.wfe.execution.QSignal;
import ru.runa.wfe.execution.process.check.FrozenProcessFilter;
import ru.runa.wfe.job.QDueDateInProcessTimerJob;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.task.QTask;

@Component
public class FrozenTokenDao extends CommonDao {

    public static final int PAGE_SIZE = 900;

    @Autowired
    private FrozenTokenFilterHandler frozenTokenFilterHandler;

    public List<CurrentToken> findByExecutionStatusIsActiveAndNodeTypesInAndNodeEnterDateLessThanAndFilter(Collection<NodeType> types,
            Date nodeEnterDate,
            int pageNumber, int pageSize, @Nullable Map<FrozenProcessFilter, String> filters) {
        QCurrentToken t = QCurrentToken.currentToken;
        BooleanExpression be = t.executionStatus.eq(ExecutionStatus.ACTIVE).and(t.nodeType.in(types).and(t.nodeEnterDate.before(nodeEnterDate)));
        if (filters != null && !filters.isEmpty()) {
            be = frozenTokenFilterHandler.getExpression(be, filters);
        }
        JPQLQuery<CurrentToken> q = queryFactory.selectFrom(t).where(be).offset(pageNumber * pageSize).limit(pageSize);
        return q.fetch();
    }

    public List<CurrentToken> findByExecutionStatusIsActiveAndNodeTypesNotInAndNodeEnterDateLessThanAndFilter(Collection<NodeType> types,
            Date nodeEnterDate,
            @Nullable Map<FrozenProcessFilter, String> filters) {
        QCurrentToken t = QCurrentToken.currentToken;
        BooleanExpression be = t.executionStatus.eq(ExecutionStatus.ACTIVE)
                .and(t.nodeType.notIn(types).and(t.nodeEnterDate.before(nodeEnterDate)));
        if (filters != null && !filters.isEmpty()) {
            be = frozenTokenFilterHandler.getExpression(be, filters);
        }
        JPQLQuery<CurrentToken> q = queryFactory.selectFrom(t).where(be);
        return q.fetch();
    }

    public List<CurrentToken> findByExecutionStatusIsActiveAndNodeTypeIsReciveMessageAndHasSignalAndFilter(
            @Nullable Map<FrozenProcessFilter, String> filters) {
        QCurrentToken t = QCurrentToken.currentToken;
        QSignal s = QSignal.signal;
        BooleanExpression be = t.nodeType.eq(NodeType.RECEIVE_MESSAGE).and(t.executionStatus.eq(ExecutionStatus.ACTIVE));
        if (filters != null && !filters.isEmpty()) {
            be = frozenTokenFilterHandler.getExpression(be, filters);
        }
        JPQLQuery<CurrentToken> q = queryFactory.select(t).from(s, t).where(be.and(s.messageSelectorsValue.contains(t.messageSelector)));
        return q.fetch();
    }

    public List<CurrentToken> findByExecutionStatusIsActiveAndNodeTypeIsSubprocessAndProcessExecutionStatusNotEndedAndFilter(
            @Nullable Map<FrozenProcessFilter, String> filters) {
        QCurrentToken t = QCurrentToken.currentToken;
        QCurrentProcess p = QCurrentProcess.currentProcess;
        QCurrentNodeProcess np = QCurrentNodeProcess.currentNodeProcess;
        BooleanExpression be = t.nodeType.in(NodeType.SUBPROCESS, NodeType.MULTI_SUBPROCESS).and(t.executionStatus.eq(ExecutionStatus.ACTIVE))
                .and(p.executionStatus.ne(ExecutionStatus.ENDED))
                // this check is here because embedded subprocesses have type NodeType.SUBPROCESS :(
                .and(JPAExpressions.select(np.count()).from(np).where(np.parentToken.eq(t).and(np.nodeId.eq(t.nodeId))).ne(0L))
                .and(JPAExpressions.select(np.count()).from(np, p)
                        .where(np.parentToken.eq(t).and(np.subProcess.eq(p)).and(p.executionStatus.ne(ExecutionStatus.ENDED))).eq(0L));
        if (filters != null && !filters.isEmpty()) {
            be = frozenTokenFilterHandler.getExpression(be, filters);
        }
        JPQLQuery<CurrentToken> q = queryFactory.select(t).from(t, p).where(t.process.eq(p).and(be));
        return q.fetch();
    }

    public List<CurrentToken> findByExecutionStatusIsActiveAndNodeTypeIsTimerAndTimerJobIsExpiredAndFilter(
            @Nullable Map<FrozenProcessFilter, String> filters) {
        QCurrentToken t = QCurrentToken.currentToken;
        QDueDateInProcessTimerJob j = QDueDateInProcessTimerJob.dueDateInProcessTimerJob;
        BooleanExpression be = t.nodeType.eq(NodeType.TIMER).and(t.executionStatus.eq(ExecutionStatus.ACTIVE))
                .and(j.process.eq(t.process)).and(j.token.eq(t)).and(j.dueDate.loe(new Date()));
        if (filters != null && !filters.isEmpty()) {
            be = frozenTokenFilterHandler.getExpression(be, filters);
        }
        JPQLQuery<CurrentToken> q = queryFactory.select(t).from(j).innerJoin(j.token, t).where(be);
        return q.fetch();
    }

    public List<CurrentToken> findByNodeIsParallelGatewayAndReactivateParentIsTrueAndFilter(int pageNumber, int pageSize,
            @Nullable Map<FrozenProcessFilter, String> filters) {
        QCurrentToken t = QCurrentToken.currentToken;
        BooleanExpression be = t.nodeType.eq(NodeType.PARALLEL_GATEWAY).and(t.ableToReactivateParent.eq(true))
                .and(t.process.executionStatus.ne(ExecutionStatus.ENDED));
        if (filters != null && !filters.isEmpty()) {
            be = frozenTokenFilterHandler.getExpression(be, filters);
        }
        JPQLQuery<CurrentToken> q = queryFactory.selectFrom(t).where(be).offset(pageNumber * pageSize).limit(pageSize);
        return q.fetch();
    }

    public List<CurrentToken> findByUnassignedTasksInActiveProcessesAndFilter(@Nullable Map<FrozenProcessFilter, String> filters) {
        QCurrentToken t = QCurrentToken.currentToken;
        QTask task = QTask.task;
        BooleanExpression be = task.executor.isNull().and(task.token.executionStatus.ne(ExecutionStatus.SUSPENDED)).and(task.token.endDate.isNull());
        if (filters != null && !filters.isEmpty()) {
            be = frozenTokenFilterHandler.getExpression(be, filters);
        }
        JPQLQuery<CurrentToken> q = queryFactory.select(t).from(task).innerJoin(task.token, t).where(be);
        return q.fetch();
    }

    public List<CurrentToken> findByTaskExecutorNamesAreInAndAssignDateLessThanAndFilter(Collection<String> executorNames, Date assignDate,
            @Nullable Map<FrozenProcessFilter, String> filters) {
        QCurrentToken t = QCurrentToken.currentToken;
        QTask task = QTask.task;
        BooleanExpression be = task.executor.name.in(executorNames).and(task.assignDate.before(assignDate));
        if (filters != null && !filters.isEmpty()) {
            be = frozenTokenFilterHandler.getExpression(be, filters);
        }
        JPQLQuery<CurrentToken> q = queryFactory.select(t).from(task).innerJoin(task.token, t).where(be);
        return q.fetch();
    }

}
