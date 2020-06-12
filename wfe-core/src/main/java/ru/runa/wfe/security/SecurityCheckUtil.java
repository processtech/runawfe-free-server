package ru.runa.wfe.security;

public class SecurityCheckUtil {
	
	public static boolean needCheckPermission(SecuredObjectType secObjectType) {
    	if (SecuredObjectType.EXECUTOR.equals(secObjectType) ) {
    		return SecurityCheckProperties.getCheckActor();
    	}
    	if (SecuredObjectType.DEFINITION.equals(secObjectType) ) {
    		return SecurityCheckProperties.getCheckProcessDefinition();
    	}
    	if (SecuredObjectType.PROCESS.equals(secObjectType) ) {
    		return SecurityCheckProperties.getCheckProcessInstance();
    	}
    	if (SecuredObjectType.REPORT.equals(secObjectType) ) {
    		return SecurityCheckProperties.getCheckReport();
    	}
    	if (SecuredObjectType.REPORTS.equals(secObjectType) ) {
    		return SecurityCheckProperties.getCheckReport();
    	}
    	if (SecuredObjectType.RELATION.equals(secObjectType) ) {
    		return SecurityCheckProperties.getCheckRelation();
    	}
    	if (SecuredObjectType.RELATIONS.equals(secObjectType) ) {
    		return SecurityCheckProperties.getCheckRelation();
    	}
    	if (SecuredObjectType.BOTSTATIONS.equals(secObjectType) ) {
    		return SecurityCheckProperties.getCheckBotStation();
    	}
    	// TODO bot, datasource
    	if (SecuredObjectType.SYSTEM.equals(secObjectType) ) {
    		return SecurityCheckProperties.getCheckSystem();
    	}
    	return true;
    }
	
}
