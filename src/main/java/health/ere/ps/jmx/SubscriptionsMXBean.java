package health.ere.ps.jmx;

@SuppressWarnings("unused")
public interface SubscriptionsMXBean {

    String OBJECT_NAME = "health.ere.ps:type=SubscriptionsMXBean";

    int getSubscriptionsAmount();
}