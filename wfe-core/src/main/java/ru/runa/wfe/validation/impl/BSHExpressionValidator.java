package ru.runa.wfe.validation.impl;

import ru.runa.wfe.commons.BSHScriptExecutor;
import ru.runa.wfe.commons.IScriptExecutor;

public class BSHExpressionValidator extends GroovyExpressionValidator {
	
	protected IScriptExecutor getScriptExecutor() {
		return new BSHScriptExecutor();
	}

}
