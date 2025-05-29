package net.exylia.commons.redis.pubsub.subcriptions;

import net.exylia.commons.redis.pubsub.RedisPubSubManager;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.Future;

/**
 * Representa una suscripción por patrón
 */
public class PatternSubscription {

    private final String pattern;
    private final JedisPubSub subscriber;
    private final Future<?> subscriptionFuture;
    private final RedisPubSubManager manager;
    private volatile boolean cancelled = false;

    public PatternSubscription(String pattern, JedisPubSub subscriber,
                               Future<?> subscriptionFuture, RedisPubSubManager manager) {
        this.pattern = pattern;
        this.subscriber = subscriber;
        this.subscriptionFuture = subscriptionFuture;
        this.manager = manager;
    }

    /**
     * Cancela la suscripción por patrón
     */
    public void cancel() {
        if (cancelled) return;

        try {
            if (subscriber.isSubscribed()) {
                subscriber.punsubscribe(pattern);
            }

            if (subscriptionFuture != null && !subscriptionFuture.isDone()) {
                subscriptionFuture.cancel(true);
            }

            cancelled = true;

        } catch (Exception e) {
            cancelled = true;
        }
    }

    /**
     * Verifica si la suscripción está activa
     */
    public boolean isActive() {
        return !cancelled && subscriber.isSubscribed() &&
                (subscriptionFuture == null || !subscriptionFuture.isDone());
    }

    /**
     * Verifica si la suscripción fue cancelada
     */
    public boolean isCancelled() {
        return cancelled;
    }

    // ==================== GETTERS ====================

    public String getPattern() {
        return pattern;
    }

    public JedisPubSub getSubscriber() {
        return subscriber;
    }
}