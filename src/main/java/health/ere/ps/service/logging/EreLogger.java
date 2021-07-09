package health.ere.ps.service.logging;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.logging.Logger;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.CDI;

import health.ere.ps.event.EreLogNotificationEvent;

public class EreLogger extends Logger {
    protected Logger externalLogger;
    protected List<SystemContext> systemContextList;
    protected String simpleLogMessage;
    protected boolean publishLogNotificationEvent;

    public enum SystemContext {
        Connector("Connector"),
        ConnectorVersion("Connector Version"),
        CardTerminal("Card Terminal"),
        SmcbCard("SMC-B Card"),
        HbaCard("eHBA Card"),
        GematikIdp("Gematik Idp"),
        GematikEprescriptionService("Gematik ePrescription Service"),
        Vau("VAU"),
        PrescriptionReceipt("Prescription Receipt"),
        WebSocket("Web Socket"),
        Muster16Parsing("Muster 16 Parsing"),
        KbvBundlesProcessing("KBV Bundles Processing"),
        KbvBundleValidation("KBV Bundle Validation"),
        SystemUpdate("System Update");

        SystemContext(String sysContext) {
            this.sysContext = sysContext;
        }

        private String sysContext;

        public String getSysContext() {
            return sysContext;
        }
    }

    /**
     * Construct a new instance.
     *
     * @param logger       the logger to decorate
     * @param categoryName the logger category name
     */
    protected EreLogger(Logger logger, String categoryName) {
        super(categoryName);
        externalLogger = logger;
    }

    public static EreLogger getLogger(Class<?> clazz) {
        Logger logger = Logger.getLogger(clazz);

        return new EreLogger(logger, logger.getName());
    }

    protected String getSimpleLogPrefix(String originalLogMessage, String defaultMessage) {
        String decoratedMessage = StringUtils.defaultString(originalLogMessage);

        if(CollectionUtils.isNotEmpty(getSystemContextList())) {
            String sysCtxPrefix = CollectionUtils.isNotEmpty(getSystemContextList()) ?
                    getSystemContextList().stream()
                            .map(ctx -> ctx.getSysContext())
                            .collect(Collectors.joining(",")) : defaultMessage;
            decoratedMessage = String.format("[%s <--> %s] <==> " +
                            StringUtils.defaultString(decoratedMessage), sysCtxPrefix,
                    StringUtils.defaultString(simpleLogMessage, "?"));
        }

        return decoratedMessage;
    }

    public EreLogger setLoggingContext(List<SystemContext> systemContextList,
                                       String simpleLogMessage,
                                       boolean publishLogNotificationEvent) {
        this.systemContextList = systemContextList;
        this.simpleLogMessage = simpleLogMessage;
        this.setPublishLogNotificationEvent(publishLogNotificationEvent);

        return this;
    }

    public EreLogger setLoggingContext(List<SystemContext> systemContextList,
                                       String simpleLogMessage) {
        return setLoggingContext(systemContextList, simpleLogMessage, false);
    }

    public EreLogger setLoggingContext(List<SystemContext> systemContextList,
                                       boolean notifyFrontEndUser) {
        return setLoggingContext(systemContextList, null, notifyFrontEndUser);
    }

    public EreLogger setLoggingContext(List<SystemContext> systemContextList) {
        this.systemContextList = systemContextList;
        this.setPublishLogNotificationEvent(false);

        return this;
    }

    public EreLogger setPublishLogNotificationEvent(boolean publishLogNotificationEvent) {
        this.publishLogNotificationEvent = publishLogNotificationEvent;

        return this;
    }


    public EreLogger setSimpleLogMessage(String simpleLogMessage) {
        this.simpleLogMessage = simpleLogMessage;
        return this;
    }

    protected List<SystemContext> getSystemContextList() {
        return systemContextList;
    }

    protected boolean isPublishLogNotificationEvent() {
        return publishLogNotificationEvent;
    }

    public EreLogger clearLoggingContext() {
        if (CollectionUtils.isNotEmpty(getSystemContextList())) {
            getSystemContextList().clear();
        }

        systemContextList = null;
        simpleLogMessage = null;
        setPublishLogNotificationEvent(false);

        return this;
    }

    @Override
    protected void doLog(Level level, String loggerClassName, Object message,
                         Object[] parameters, Throwable thrown) {

        String logMessage = MessageFormat.format((String)message, parameters);
        String decoratedMessage = getSimpleLogPrefix(logMessage,
                StringUtils.defaultString(loggerClassName));

        externalLogger.log(loggerClassName, level, decoratedMessage, parameters, thrown);

        publishLogNotification(logMessage, level.name(),
                Arrays.asList(ExceptionUtils.getStackFrames(thrown)));
    }

    @Override
    protected void doLogf(Level level, String loggerClassName, String format,
                          Object[] parameters, Throwable thrown) {
        String logMessage = String.format(format, parameters);
        String decoratedMessage = getSimpleLogPrefix(format,
                StringUtils.defaultString(loggerClassName));

        externalLogger.logf(loggerClassName, level, thrown, decoratedMessage, parameters);

        publishLogNotification(logMessage, level.name(),
                Arrays.asList(ExceptionUtils.getStackFrames(thrown)));
    }

    @Override
    public boolean isEnabled(Level level) {
        return externalLogger.isEnabled(level);
    }

    protected String getSimpleLogMessage() {
        return simpleLogMessage;
    }

    protected void publishLogNotification(String logMessage, String status,
                                          List<String> errorDetails) {
        if(isPublishLogNotificationEvent()) {
            List<String> sysContexts = CollectionUtils.isNotEmpty(getSystemContextList()) ?
                    getSystemContextList().stream()
                            .map(ctx -> ctx.getSysContext())
                            .collect(Collectors.toList()) : List.of();

            EreLogNotificationEvent event =
                    new EreLogNotificationEvent(sysContexts,
                            StringUtils.defaultString(getSimpleLogMessage()),
                            StringUtils.defaultString(status),
                            StringUtils.defaultString(logMessage),
                            ListUtils.defaultIfNull(errorDetails, null));
            Event<EreLogNotificationEvent> ereLogNotificationEvent =
                    CDI.current().select(Event.class).get();
            ereLogNotificationEvent.fireAsync(event);
        }
    }
}
