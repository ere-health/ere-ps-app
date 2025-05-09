package health.ere.ps.jmx;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class PsMXBeanManager {

    private static final Logger log = Logger.getLogger(PsMXBeanManager.class.getName());

    private static final MBeanServer MBEAN_SERVER = ManagementFactory.getPlatformMBeanServer();

    public static void registerMXBean(String name, Object mxbean) {
        try {
            ObjectName objectName = new ObjectName(name);
            if (MBEAN_SERVER.isRegistered(objectName)) {
                log.warning(String.format("MXBean %s is already registered", name));
            } else {
                MBEAN_SERVER.registerMBean(mxbean, objectName);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, String.format("JMX Bean for %s was not created", name), e);
        }
    }

    public static void registerMXBean(Object mbean) {
        var interfaces = mbean.getClass().getInterfaces();
        var mXBeanInterface = Stream.of(interfaces)
                .filter(interfaceClass ->
                        interfaceClass.getPackageName().startsWith("health.ere") &&
                                interfaceClass.getSimpleName().endsWith("MXBean")
                ).findAny().orElseThrow();
        var objectName = "health.ere.ps:type=" + mXBeanInterface.getSimpleName();
        registerMXBean(objectName, mbean);
    }

    public static void unregisterMXBean(String name) {
        try {
            MBEAN_SERVER.unregisterMBean(new ObjectName(name));
        } catch (Exception e) {
            log.log(Level.SEVERE, String.format("Unable to unregister JMX Bean %s", name), e);
        }
    }

    public static <T> T getMXBean(String name, Class<T> clazz) {
        try {
            ObjectName objectName = new ObjectName(name);
            return MBEAN_SERVER.isRegistered(objectName) ? JMX.newMBeanProxy(MBEAN_SERVER, objectName, clazz) : null;
        } catch (Exception e) {
            log.log(Level.FINE, String.format("Error accessing JMX Bean %s", name), e);
            return null;
        }
    }

    public static void registerMXBeans(Map<String, Object> objectNameToMXBean) {
        objectNameToMXBean.forEach(PsMXBeanManager::registerMXBean);
    }
}