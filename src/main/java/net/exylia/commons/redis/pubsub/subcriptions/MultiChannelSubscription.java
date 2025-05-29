package net.exylia.commons.redis.pubsub.subcriptions;

import net.exylia.commons.redis.pubsub.RedisPubSubManager;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.Future;

/**
 * Representa una suscripción a múltiples canales
 */
public class MultiChannelSubscription {

    private final String[] channels;
    private final JedisPubSub subscriber;
    private final Future<?> subscriptionFuture;
    private final RedisPubSubManager manager;
    private volatile boolean cancelled = false;

    public MultiChannelSubscription(String[] channels, JedisPubSub subscriber,
                                    Future<?> subscriptionFuture, RedisPubSubManager manager) {
        this.channels = channels.clone();
        this.subscriber = subscriber;
        this.subscriptionFuture = subscriptionFuture;
        this.manager = manager;
    }

    /**
     * Cancela la suscripción a todos los canales
     */
    public void cancel() {
        if (cancelled) return;

        try {
            if (subscriber.isSubscribed()) {
                subscriber.unsubscribe(channels);
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
     * Cancela la suscripción a un canal específico
     */
    public void cancel(String channel) {
        try {
            if (subscriber.isSubscribed()) {
                subscriber.unsubscribe(channel);
            }
        } catch (Exception e) {
            // Ignorar errores al cancelar canal específico
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

    public String[] getChannels() {
        return channels.clone();
    }

    public JedisPubSub getSubscriber() {
        return subscriber;
    }
}