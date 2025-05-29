package net.exylia.commons.redis.pubsub.subcriptions;

import net.exylia.commons.redis.pubsub.RedisPubSubManager;
import redis.clients.jedis.JedisPubSub;
import java.util.concurrent.Future;

/**
 * Representa una suscripción a un canal de Redis
 */
public class RedisSubscription {

    private final String channel;
    private final JedisPubSub subscriber;
    private final Future<?> subscriptionFuture;
    private final RedisPubSubManager manager;
    private volatile boolean cancelled = false;

    public RedisSubscription(String channel, JedisPubSub subscriber,
                             Future<?> subscriptionFuture, RedisPubSubManager manager) {
        this.channel = channel;
        this.subscriber = subscriber;
        this.subscriptionFuture = subscriptionFuture;
        this.manager = manager;
    }

    /**
     * Cancela la suscripción
     */
    public void cancel() {
        if (cancelled) return;

        try {
            if (subscriber.isSubscribed()) {
                subscriber.unsubscribe(channel);
            }

            if (subscriptionFuture != null && !subscriptionFuture.isDone()) {
                subscriptionFuture.cancel(true);
            }

            cancelled = true;

        } catch (Exception e) {
            // Error al cancelar, pero marcamos como cancelado de todas formas
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

    public String getChannel() {
        return channel;
    }

    public JedisPubSub getSubscriber() {
        return subscriber;
    }
}