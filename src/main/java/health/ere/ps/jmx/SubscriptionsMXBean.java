package health.ere.ps.jmx;

@SuppressWarnings("unused")
public interface SubscriptionsMXBean {

    String OBJECT_NAME = "health.ere.ps:type=SubscriptionsMXBean";

    static SubscriptionsMXBean getInstance() {
        return PsMXBeanManager.getMXBean(SubscriptionsMXBean.OBJECT_NAME, SubscriptionsMXBean.class);
    }

    int getSubscriptionsAmount();
}
