package ru.runa.af.web.form;

import lombok.Getter;
import lombok.Setter;
import org.apache.struts.action.ActionForm;

/**
 * 
 * @struts:form name = "frozenProcessesForm"
 */
@Getter
@Setter
public class FrozenProcessesForm extends ActionForm {

    private static final long serialVersionUID = 2717627162657848349L;
    private static final String EMPTY_STRING = "";
    // Checkbox and input values
    private boolean searchFrozenInParallelGateways;
    private boolean searchFrozenInUnexpectedNodes;
    private boolean searchFrozenInTaskNodes;
    private boolean searchFrozenInTimerNodes;
    private boolean searchFrozenSubprocesses;
    private boolean searchFrozenBySignalTimeExceeded;
    private boolean searchFrozenBySignal;
    private String searchFrozenBySignalTimeExceededTimeLapse;
    private String searchFrozenInTaskNodesTimeThreshold;
    // Filters
    private String processName = EMPTY_STRING;
    private String processVersion = EMPTY_STRING;
    private String firstFilterDate = EMPTY_STRING;
    private String secondFilterDate = EMPTY_STRING;
    private String nodeType = EMPTY_STRING;

}