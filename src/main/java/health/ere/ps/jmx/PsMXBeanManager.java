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

    public static void registerMXBean(String name, Object mbean) {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        register(server, name, mbean);
    }

    public static void registerMXBean(Object mbean) {
        var interfaces = mbean.getClass().getInterfaces();
        var mXBeanInterface = Stream.of(interfaces)
                .filter(interfaceClass ->
                        interfaceClass.getPackageName().startsWith("health.ere") &&
                                interfaceClass.getSimpleName().endsWith("MXBean")
                ).findAny().orElseThrow();
        var objectName = "health.ere.ps:type=" + mXBeanInterface.getSimpleName();
        var server = ManagementFactory.getPlatformMBeanServer();
        register(server, objectName, mbean);
    }

    public static void unregisterMXBean(String name) {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            server.unregisterMBean(new ObjectName(name));
        } catch (Exception e) {
            log.log(Level.SEVERE, String.format("Unable to unregister JMX Bean %s", name), e);
        }
    }

    public static <T> T getMXBean(String name, Class<T> clazz) {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            return JMX.newMBeanProxy(server, new ObjectName(name), clazz);
        } catch (Exception e) {
            log.log(Level.SEVERE, String.format("JMX Bean %s is not found", name), e);
            return null;
        }
    }

    public static void registerMXBeans(Map<String, Object> objectNameToMXBean) {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        objectNameToMXBean.forEach((name, mbean) -> register(server, name, mbean));
    }

    private static void register(MBeanServer server, String name, Object mxbean) {
        try {
            ObjectName objectName = new ObjectName(name);
            if (server.isRegistered(objectName)) {
                log.warning(String.format("MXBean %s is already registered", name));
            } else {
                server.registerMBean(mxbean, objectName);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, String.format("JMX Bean for %s was not created", name), e);
        }
    }
}
