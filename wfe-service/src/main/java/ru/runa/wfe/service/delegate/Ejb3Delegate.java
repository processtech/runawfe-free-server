package ru.runa.wfe.service.delegate;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Properties;
import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.service.utils.EjbProperties;

@CommonsLog
public abstract class Ejb3Delegate {
    public static final String EJB_REMOTE = "remote";
    private static final String EJB_LOCAL = "";
    private static final String WFE_SERVICE_JAR_NAME = "wfe-service";
    private static Map<String, InitialContext> initialContexts = Maps.newHashMap();
    private static Map<String, Map<String, Object>> services = Maps.newHashMap();
    private final String ejbType;
    private final String jarName;
    private final String beanName;
    private final String localInterfaceClassName;
    private final String remoteInterfaceClassName;
    private String customProviderUrl;

    /**
     * Creates delegate only for remote usage.
     *
     * @param beanName
     *            EJB bean name
     * @param remoteInterfaceClass
     *            EJB @Remote class
     */
    public Ejb3Delegate(String beanName, Class<?> remoteInterfaceClass, String jarName) {
        this.beanName = beanName;
        localInterfaceClassName = null;
        remoteInterfaceClassName = remoteInterfaceClass.getName();
        this.jarName = jarName;
        this.ejbType = EJB_REMOTE;
    }

    /**
     * Creates delegate only for remote usage.
     *
     * @param beanName
     *            EJB bean name
     * @param remoteInterfaceClass
     *            EJB @Remote class
     */
    public Ejb3Delegate(String beanName, Class<?> remoteInterfaceClass) {
        this(beanName, remoteInterfaceClass, WFE_SERVICE_JAR_NAME);
    }

    /**
     * Creates delegate based on base interface class (implicit assumptions
     * about @Local, @Remote interface and EJB bean naming)
     */
    public Ejb3Delegate(Class<?> baseInterfaceClass) {
        beanName = baseInterfaceClass.getSimpleName() + "Bean";
        localInterfaceClassName = "ru.runa.wfe.service.decl." + baseInterfaceClass.getSimpleName() + "Local";
        remoteInterfaceClassName = "ru.runa.wfe.service.decl." + baseInterfaceClass.getSimpleName() + "Remote";
        jarName = WFE_SERVICE_JAR_NAME;
        this.ejbType = EjbProperties.getConnectionType();
    }

    protected String getCustomProviderUrl() {
        return customProviderUrl;
    }

    public void setCustomProviderUrl(String customProviderUrl) {
        this.customProviderUrl = customProviderUrl;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getService() {
        String providerUrl = MoreObjects.firstNonNull(getCustomProviderUrl(), EJB_LOCAL);
        Map<String, Object> providerServices = services.get(providerUrl);
        if (providerServices == null) {
            providerServices = Maps.newHashMap();
            services.put(providerUrl, providerServices);
        }
        if (!providerServices.containsKey(beanName)) {
            Map<String, String> variables = Maps.newHashMap();
            variables.put("jar.name", jarName);
            variables.put("jar.version", SystemProperties.getVersion());
            variables.put("bean.name", beanName);
            variables.put("ejb.type", ejbType);
            String interfaceClassName = EJB_REMOTE.equals(ejbType) ? remoteInterfaceClassName : localInterfaceClassName;
            variables.put("interface.class.name", interfaceClassName);
            String jndiNameFormat;
            if (!Strings.isNullOrEmpty(providerUrl) && EjbProperties.useJbossEjbClientForRemoting()) {
                jndiNameFormat = EjbProperties.getJbossEjbClientJndiNameFormat();
            } else {
                jndiNameFormat = EjbProperties.getJndiNameFormat();
            }
            String jndiName = ExpressionEvaluator.substitute(jndiNameFormat, variables);
            try {
                Object service = getInitialContext().lookup(jndiName);
                providerServices.put(beanName, service);
            } catch (NamingException e) {
                throw new InternalApplicationException("Unable to locate bean by jndi name '" + jndiName + "'", e);
            }
        }
        return (T) providerServices.get(beanName);
    }

    private InitialContext getInitialContext() {
        String providerUrl = MoreObjects.firstNonNull(getCustomProviderUrl(), EJB_LOCAL);
        if (!initialContexts.containsKey(providerUrl)) {
            try {
                Properties properties;
                if (!Objects.equal(EJB_LOCAL, providerUrl) || EjbProperties.isJbossEjbClientStaticEnabled()) {
                    properties = ClassLoaderUtil.getProperties("jndi.properties", false);
                    if (EjbProperties.useJbossEjbClientForRemoting()) {
                        String port = EjbProperties.getJbossEjbClientPort();
                        String hostname;
                        if (providerUrl.contains(":")) {
                            int colonIndex = providerUrl.indexOf(":");
                            port = providerUrl.substring(colonIndex + 1);
                            hostname = providerUrl.substring(0, colonIndex);
                        } else {
                            hostname = providerUrl;
                        }
                        String name = "n_" + hostname;
                        properties.put("remote.connections", name);
                        properties.put("remote.connection." + name + ".host", hostname);
                        properties.put("remote.connection." + name + ".port", port);
                        properties.put("remote.connection." + name + ".username", EjbProperties.getJbossEjbClientUsername());
                        properties.put("remote.connection." + name + ".password", EjbProperties.getJbossEjbClientPassword());
                    } else {
                        properties.put(Context.PROVIDER_URL, providerUrl);
                    }
                    log.debug("Trying to obtain remote connection for '" + providerUrl + "' using " + properties);
                } else {
                    properties = new Properties();
                }
                initialContexts.put(providerUrl, new InitialContext(properties));
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
        return initialContexts.get(providerUrl);
    }

    protected RuntimeException handleException(Exception e) {
        if (e instanceof EJBException && e.getCause() != null) {
            return Throwables.propagate(e.getCause());
        }
        return Throwables.propagate(e);
    }
}
