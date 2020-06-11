package ru.runa.wfe.security;

import ru.runa.wfe.commons.PropertyResources;

public class SecurityCheckProperties {
	private static final PropertyResources RESOURCES = new PropertyResources("security.check.properties");

    public static PropertyResources getResources() {
        return RESOURCES;
    }
    
    public static boolean getCheckActor() {
    	return RESOURCES.getBooleanProperty("security.check.object.actor", false);
    }
    
    public static boolean getCheckProcessDefinition() {
    	return RESOURCES.getBooleanProperty("security.check.object.process.definition", false);
    }
    
    public static boolean getCheckProcessInstance() {
    	return RESOURCES.getBooleanProperty("security.check.object.process.instance", false);
    }
    
    public static boolean getCheckReport() {
    	return RESOURCES.getBooleanProperty("security.check.object.report", false);
    }
    
    public static boolean getCheckRelation() {
    	return RESOURCES.getBooleanProperty("security.check.object.relation", false);
    }
    
    public static boolean getCheckBotStation() {
    	return RESOURCES.getBooleanProperty("security.check.object.botstation", false);
    }
    
    public static boolean getCheckBot() {
    	return RESOURCES.getBooleanProperty("security.check.object.bot", false);
    }
    
    public static boolean getCheckDataSource() {
    	return RESOURCES.getBooleanProperty("security.check.object.datasource", false);
    }
    
    public static boolean getCheckSystem() {
    	return RESOURCES.getBooleanProperty("security.check.object.system", false);
    }
}
