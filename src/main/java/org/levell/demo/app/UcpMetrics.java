package org.levell.demo.app;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.jetbrains.annotations.NotNull;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * {@link MeterBinder} for UCP.
 */
public class UcpMetrics implements MeterBinder, AutoCloseable {

    private static final String JMX_DOMAIN = "oracle.ucp.admin.UniversalConnectionPoolMBean";

    private final MBeanServer mBeanServer;

    private final Iterable<Tag> tags;

    private final Set<NotificationListener> notificationListeners = ConcurrentHashMap.newKeySet();

    public UcpMetrics() {
        this.mBeanServer = getMBeanServer();
        tags = Collections.emptyList();
    }

    private static MBeanServer getMBeanServer() {
        List<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
        if (!mBeanServers.isEmpty()) {
            return mBeanServers.get(0);
        }
        return ManagementFactory.getPlatformMBeanServer();
    }

    @Override
    public void bindTo(@NotNull MeterRegistry registry) {
        registerMetricsEventually((name, tags) -> {
            Gauge
                    .builder("ucp.connections.available.count", mBeanServer,
                            s -> safeDouble(() -> s.getAttribute(name, "availableConnectionsCount")))
                    .baseUnit(BaseUnits.THREADS)
                    .description("The number of available connections in the pool")
                    .tags(tags)
                    .register(registry);
        });
    }

    private Iterable<Tag> nameTag(ObjectName name) {
        String nameTagValue = name.getKeyProperty("name");
        if (nameTagValue != null) {
            return Tags.of("name", nameTagValue.replaceAll("\"", ""));
        }
        return Collections.emptyList();
    }

    private void registerMetricsEventually(BiConsumer<ObjectName, Iterable<Tag>> perObject) {
        Set<ObjectName> objectNames = this.mBeanServer.queryNames(getObjectName(), null);
        if (!objectNames.isEmpty()) {
            objectNames.forEach(objectName -> perObject.accept(objectName, Tags.concat(tags, nameTag(objectName))));
            return;
        }

        NotificationListener notificationListener = new NotificationListener() {
            @Override
            public void handleNotification(Notification notification, Object handback) {
                MBeanServerNotification mBeanServerNotification = (MBeanServerNotification) notification;
                ObjectName objectName = mBeanServerNotification.getMBeanName();
                perObject.accept(objectName, Tags.concat(tags, nameTag(objectName)));
                if (getObjectName().isPattern()) {
                    // patterns can match multiple MBeans so don't remove listener
                    return;
                }
                try {
                    mBeanServer.removeNotificationListener(MBeanServerDelegate.DELEGATE_NAME, this);
                    notificationListeners.remove(this);
                }
                catch (InstanceNotFoundException | ListenerNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        notificationListeners.add(notificationListener);

        NotificationFilter notificationFilter = notification -> {
            if (!MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(notification.getType())) {
                return false;
            }

            ObjectName objectName = ((MBeanServerNotification) notification).getMBeanName();
            return getObjectName().apply(objectName);
        };

        try {
            mBeanServer.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME, notificationListener,
                    notificationFilter, null);
        }
        catch (InstanceNotFoundException e) {
            throw new RuntimeException("Error registering MBean listener", e);
        }
    }

    private ObjectName getObjectName() {
        try {
            return new ObjectName("oracle.ucp.admin.UniversalConnectionPoolMBean:name=UniversalConnectionPoolManager*,poolName=*");
//            return new ObjectName(JMX_DOMAIN + ":type=UniversalConnectionPoolManager*,name=*");
        }
        catch (MalformedObjectNameException e) {
            throw new RuntimeException("Error registering Tomcat JMX based metrics", e);
        }
    }

    private double safeDouble(Callable<Object> callable) {
        try {
            return Double.parseDouble(callable.call().toString());
        }
        catch (Exception e) {
            return Double.NaN;
        }
    }

    @Override
    public void close() {
        for (NotificationListener notificationListener : this.notificationListeners) {
            try {
                this.mBeanServer.removeNotificationListener(MBeanServerDelegate.DELEGATE_NAME, notificationListener);
            }
            catch (InstanceNotFoundException | ListenerNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
