package health.ere.ps.jmx;

public class SubscriptionsMXBeanImpl implements SubscriptionsMXBean {

    private final int subscriptionsAmount;

    public SubscriptionsMXBeanImpl(int subscriptionsAmount) {
        this.subscriptionsAmount = subscriptionsAmount;
    }

    @Override
    public int getSubscriptionsAmount() {
        return subscriptionsAmount;
    }
}
