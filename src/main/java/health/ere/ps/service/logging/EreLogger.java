package health.ere.ps.service.logging;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.CDI;

import health.ere.ps.event.EreLogNotificationEvent;

/**
 * This class decorates a JBoss Logger to allow for enhancing log statements with system context
 * information and to provide complementary end user friendly log and notification messages.
 *
 * This logger also allows for publishing log notification events which can be subscribed to and
 * processed by interested event handlers.
 *
 * The {@link EreLogNotificationEvent#toString()} method renders a json string of the
 * {@link EreLogNotificationEvent} event notification object that has the following
 * format as shown in the sample below.
 *
 * The json representation of the event notification object can be rendered as required by the
 * front-end component of the application to advise the end user about the processing state of
 * the application.
 *
 *  {
 *     "systemContextList":["KBV Bundles Processing","KBV Bundle Validation"],
 *     "simpleLogMessage":"Bundle validation failure",
 *     "status":"ERROR",
 *     "logMessage":"Patient First name, Patient last name, Insurance Policy Number, (Medication String 1), …",
 *     "logMessageDetails":[“Validation error 1”, “Validation error 2”, “Validation error 3”, …]
 *  }
 *
 *  The supported status values are those provided by the Logger class which are as follows:
 *
 *  FATAL
 *  ERROR
 *  WARN
 *  INFO
 *  DEBUG
 *  TRACE
 *
 *  Note that if the log details is manually set via the {@link EreLogger#setLogDetails(List)}
 *  method call while an exception object is logged, the manually set log details will override
 *  the logged exception object and its contents will appear in the
 *  {@link EreLogNotificationEvent} object's logMessageDetails[] list instead of the exception
 *  object's message.
 *
 *  This functionality is used to substitute end user friendly notification message details
 *  in the front-end UI for cryptic exception stack traces.
 *
 *  The exception stack trace details will still appear in the log file instead of the
 *  manually set log details if an exception object is being logged.
 */
public class EreLogger extends Logger {
    protected Logger externalLogger;
    protected List<SystemContext> systemContextList;
    protected String simpleLogMessage;
    protected String logMessage;
    protected List<String> logDetails;
    protected String status;
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

    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);

        return new EreLogger(logger, logger.getName());
    }

    public static Logger getLogger(String name, String suffix) {
        Logger logger = Logger.getLogger(name, suffix);

        return new EreLogger(logger, logger.getName());
    }

    public static EreLogger getLogger(Class<?> clazz) {
        Logger logger = Logger.getLogger(clazz);

        return new EreLogger(logger, logger.getName());
    }

    public static Logger getLogger(Class<?> clazz, String suffix) {
        Logger logger = Logger.getLogger(clazz, suffix);

        return new EreLogger(logger, logger.getName());
    }

    protected String getSimpleLogPrefix(String originalLogMessage, String defaultMessage) {

        if(CollectionUtils.isNotEmpty(getSystemContextList())) {
            String sysCtxPrefix = CollectionUtils.isNotEmpty(getSystemContextList()) ?
                    getSystemContextList().stream()
                            .map(ctx -> ctx.getSysContext())
                            .collect(Collectors.joining(",")) :
                    StringUtils.defaultString(defaultMessage);
            String decoratedMessage = String.format("[%s <--> %s] <==> " +
                            StringUtils.defaultString(originalLogMessage), sysCtxPrefix,
                    StringUtils.defaultString(simpleLogMessage, "?"));

            return decoratedMessage;
        }

        return originalLogMessage;
    }

    /**
     * Set a list of itemised log details.
     *
     * @param logDetails the list of extended itemised log details.
     *
     * @return the current EreLogger object associated with this method call.
     */
    public EreLogger setLogDetails(List<String> logDetails) {
        this.logDetails = logDetails;

        return this;
    }

    protected List<String> getLogDetails() {
        return logDetails;
    }

    public EreLogger setStatus(String status) {
        this.status = status;

        return this;
    }

    public String getStatus() {
        return status;
    }

    public EreLogger setLogMessage(String logMessage) {
        this.logMessage = logMessage;

        return this;
    }

    protected String getLogMessage() {
        return logMessage;
    }

    /**
     * Set the list of system context information associated with the information item to be logged
     * or sent in a log notification event.
     *
     * @param systemContextList the list of system components, or process contexts.
     * @param simpleLogMessage an end user friendly and easily understandable log or notification
     *                         message.
     * @param publishLogNotificationEvent true - publish notification event, false - do
     *                                    not publish notification event.
     * @return the current EreLogger object associated with this method call.
     *
     */
    public EreLogger setLoggingContext(List<SystemContext> systemContextList,
                                       String simpleLogMessage,
                                       boolean publishLogNotificationEvent) {
        this.systemContextList = systemContextList;
        this.simpleLogMessage = simpleLogMessage;
        this.setPublishLogNotificationEvent(publishLogNotificationEvent);

        return this;
    }

    /**
     * Set the list of system context information associated with the information item to be logged
     * or sent in a log notification event.
     *
     * Sets the boolean flag associated with publishing a notification event to false.
     *
     * @param systemContextList the list of system components, or process contexts.
     * @param simpleLogMessage an end user friendly and easily understandable log or notification
     *                         message.
     * @return the current EreLogger object associated with this method call.
     *
     */
    public EreLogger setLoggingContext(List<SystemContext> systemContextList,
                                       String simpleLogMessage) {
        return setLoggingContext(systemContextList, simpleLogMessage, false);
    }

    /**
     * Set the list of system context information associated with the information item to be logged
     * or sent in a log notification event.
     *
     * Sets the boolean flag associated with publishing a notification event to false.
     *
     * Does not assign or set a simple log message.
     *
     * @param systemContextList the list of system components, or process contexts.
     *
     * @return the current EreLogger object associated with this method call.
     *
     */
    public EreLogger setLoggingContext(List<SystemContext> systemContextList,
                                       boolean notifyFrontEndUser) {
        return setLoggingContext(systemContextList, null, notifyFrontEndUser);
    }

    /**
     * Set the list of system context information associated with the information item to be logged
     * or sent in a log notification event.
     *
     * Sets the boolean flag associated with publishing a notification event to false.
     *
     * Does not assign or set a simple log message.
     *
     * @param systemContextList the list of system components, or process contexts.
     *
     * @return the current EreLogger object associated with this method call.
     *
     */
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

    /**
     * Clears any set systems context and log details list, sets publish notification event boolean
     * flag to false, and clears any set simple log message.
     *
     */
    public EreLogger clearLoggingContext() {
        setLogDetails(null);
        setSimpleLogMessage(null);
        setLoggingContext(null);
        setStatus(null);
        setLogMessage(null);
        setPublishLogNotificationEvent(false);

        return this;
    }

    private void handlePostLogging(String loggingMsg, String logStatus, Throwable thrown) {
        setLogDetails(CollectionUtils.isNotEmpty(getLogDetails())? getLogDetails() :
                thrown != null? List.of(StringUtils.defaultString(thrown.getMessage())) :
                        List.of());
        setLogMessage(loggingMsg);
        setStatus(logStatus);

        publishLogNotification();

        clearLoggingContext();
    }

    @Override
    protected void doLog(Level level, String loggerClassName, Object message,
                         Object[] parameters, Throwable thrown) {

        String filledLogMessage = MessageFormat.format((String)message, parameters);
        String decoratedMessage = getSimpleLogPrefix(filledLogMessage,
                StringUtils.defaultString(loggerClassName));

        externalLogger.log(loggerClassName, level, decoratedMessage, parameters, thrown);

        handlePostLogging(filledLogMessage, level.name(), thrown);
    }

    @Override
    protected void doLogf(Level level, String loggerClassName, String format,
                          Object[] parameters, Throwable thrown) {
        String filledLogMessage = String.format(format, parameters);
        String decoratedMessage = getSimpleLogPrefix(filledLogMessage,
                StringUtils.defaultString(loggerClassName));

        externalLogger.logf(loggerClassName, level, thrown, decoratedMessage, parameters);

        handlePostLogging(filledLogMessage, level.name(), thrown);
    }

    @Override
    public boolean isEnabled(Level level) {
        return externalLogger.isEnabled(level);
    }

    protected String getSimpleLogMessage() {
        return simpleLogMessage;
    }

    protected void publishLogNotification() {
        if(isPublishLogNotificationEvent()) {
            List<String> sysContexts = CollectionUtils.isNotEmpty(getSystemContextList()) ?
                    getSystemContextList().stream()
                            .map(ctx -> ctx.getSysContext())
                            .collect(Collectors.toList()) : List.of();

            EreLogNotificationEvent event =
                    new EreLogNotificationEvent(sysContexts,
                            StringUtils.defaultString(getSimpleLogMessage()),
                            StringUtils.defaultString(getStatus()),
                            StringUtils.defaultString(getLogMessage()),
                            ListUtils.defaultIfNull(getLogDetails(), null));
            Event<EreLogNotificationEvent> ereLogNotificationEvent =
                    CDI.current().select(Event.class).get();

            ereLogNotificationEvent.fireAsync(event);
        }
    }
}
