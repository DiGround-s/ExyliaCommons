package net.exylia.commons.redis.pubsub;

import net.exylia.commons.redis.connection.RedisConnectionManager;
import net.exylia.commons.redis.pubsub.subcriptions.MultiChannelSubscription;
import net.exylia.commons.redis.pubsub.subcriptions.PatternSubscription;
import net.exylia.commons.redis.pubsub.subcriptions.RedisSubscription;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static net.exylia.commons.utils.DebugUtils.logError;
import static net.exylia.commons.utils.DebugUtils.logInfo;

/**
 * Gestor de Pub/Sub para Redis
 * Maneja la publicación y suscripción a canales de Redis
 */
public class RedisPubSubManager {

    private final RedisConnectionManager connectionManager;
    private final ConcurrentHashMap<String, RedisSubscriber> subscribers;
    private final ExecutorService executorService;
    private volatile boolean initialized = false;

    public RedisPubSubManager(RedisConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.subscribers = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "ExyliaRedis-PubSub");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Inicializa el sistema Pub/Sub
     */
    public synchronized void initialize() {
        if (initialized) return;

        try {
            initialized = true;
            logInfo("Sistema Pub/Sub de Redis inicializado");
        } catch (Exception e) {
            logError("Error al inicializar Pub/Sub: " + e.getMessage());
            throw new RuntimeException("Fallo al inicializar Pub/Sub", e);
        }
    }

    /**
     * Publica un mensaje en un canal
     */
    public void publish(String channel, String message) {
        if (!initialized) {
            throw new IllegalStateException("PubSubManager no está inicializado");
        }

        executorService.submit(() -> {
            try (Jedis jedis = connectionManager.getConnection()) {
                jedis.publish(channel, message);
            } catch (Exception e) {
                logError("Error publicando mensaje en canal '" + channel + "': " + e.getMessage());
            }
        });
    }

    /**
     * Publica un mensaje en un canal de forma síncrona
     */
    public long publishSync(String channel, String message) {
        if (!initialized) {
            throw new IllegalStateException("PubSubManager no está inicializado");
        }

        try (Jedis jedis = connectionManager.getConnection()) {
            return jedis.publish(channel, message);
        } catch (Exception e) {
            logError("Error publicando mensaje síncrono en canal '" + channel + "': " + e.getMessage());
            return 0;
        }
    }

    /**
     * Se suscribe a un canal
     */
    public RedisSubscription subscribe(String channel, Consumer<String> messageHandler) {
        return subscribe(channel, messageHandler, null, null);
    }

    /**
     * Se suscribe a un canal con handlers personalizados
     */
    public RedisSubscription subscribe(String channel,
                                       Consumer<String> messageHandler,
                                       Runnable onSubscribe,
                                       Runnable onUnsubscribe) {
        if (!initialized) {
            throw new IllegalStateException("PubSubManager no está inicializado");
        }

        RedisSubscriber subscriber = new SingleChannelSubscriber(
                channel, messageHandler, onSubscribe, onUnsubscribe
        );

        Future<?> future = executorService.submit(() -> {
            try (Jedis jedis = connectionManager.getConnection()) {
                jedis.subscribe(subscriber, channel);
            } catch (Exception e) {
                if (!subscriber.isUnsubscribed()) {
                    logError("Error en suscripción al canal '" + channel + "': " + e.getMessage());
                }
            }
        });

        subscribers.put(channel, subscriber);

        return new RedisSubscription(channel, subscriber, future, this);
    }

    /**
     * Se suscribe a múltiples canales
     */
    public MultiChannelSubscription subscribeMultiple(String[] channels,
                                                      Consumer<ChannelMessage> messageHandler) {
        return subscribeMultiple(channels, messageHandler, null, null);
    }

    /**
     * Se suscribe a múltiples canales con handlers personalizados
     */
    public MultiChannelSubscription subscribeMultiple(String[] channels,
                                                      Consumer<ChannelMessage> messageHandler,
                                                      Consumer<String> onSubscribe,
                                                      Consumer<String> onUnsubscribe) {
        if (!initialized) {
            throw new IllegalStateException("PubSubManager no está inicializado");
        }

        MultiChannelSubscriber subscriber = new MultiChannelSubscriber(
                messageHandler, onSubscribe, onUnsubscribe
        );

        Future<?> future = executorService.submit(() -> {
            try (Jedis jedis = connectionManager.getConnection()) {
                jedis.subscribe(subscriber, channels);
            } catch (Exception e) {
                if (!subscriber.isUnsubscribed()) {
                    logError("Error en suscripción múltiple: " + e.getMessage());
                }
            }
        });

        // Registrar suscriptor para cada canal
        for (String channel : channels) {
            subscribers.put(channel, subscriber);
        }

        return new MultiChannelSubscription(channels, subscriber, future, this);
    }

    /**
     * Se suscribe a patrones de canales
     */
    public PatternSubscription subscribePattern(String pattern,
                                                Consumer<PatternMessage> messageHandler) {
        return subscribePattern(pattern, messageHandler, null, null);
    }

    /**
     * Se suscribe a patrones de canales con handlers personalizados
     */
    public PatternSubscription subscribePattern(String pattern,
                                                Consumer<PatternMessage> messageHandler,
                                                Consumer<String> onSubscribe,
                                                Consumer<String> onUnsubscribe) {
        if (!initialized) {
            throw new IllegalStateException("PubSubManager no está inicializado");
        }

        PatternSubscriber subscriber = new PatternSubscriber(
                pattern, messageHandler, onSubscribe, onUnsubscribe
        );

        Future<?> future = executorService.submit(() -> {
            try (Jedis jedis = connectionManager.getConnection()) {
                jedis.psubscribe(subscriber, pattern);
            } catch (Exception e) {
                if (!subscriber.isUnsubscribed()) {
                    logError("Error en suscripción por patrón '" + pattern + "': " + e.getMessage());
                }
            }
        });

        return new PatternSubscription(pattern, subscriber, future, this);
    }

    /**
     * Cancela suscripción de un canal
     */
    public void unsubscribe(String channel) {
        RedisSubscriber subscriber = subscribers.remove(channel);
        if (subscriber != null && !subscriber.isUnsubscribed()) {
            subscriber.unsubscribe(channel);
        }
    }

    /**
     * Cancela todas las suscripciones
     */
    public void unsubscribeAll() {
        for (RedisSubscriber subscriber : subscribers.values()) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.unsubscribe();
            }
        }
        subscribers.clear();
    }

    /**
     * Cierra el sistema Pub/Sub
     */
    public synchronized void shutdown() {
        if (!initialized) return;

        logInfo("Cerrando sistema Pub/Sub...");

        try {
            // Cancelar todas las suscripciones
            unsubscribeAll();

            // Cerrar executor
            executorService.shutdown();

            initialized = false;
            logInfo("Sistema Pub/Sub cerrado correctamente");

        } catch (Exception e) {
            logError("Error al cerrar Pub/Sub: " + e.getMessage());
        }
    }

    /**
     * Verifica si está inicializado
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Obtiene el número de suscripciones activas
     */
    public int getActiveSubscriptions() {
        return subscribers.size();
    }

    // ==================== CLASES INTERNAS ====================

    /**
     * Clase base para suscriptores de Redis
     */
    public static abstract class RedisSubscriber extends JedisPubSub {
        public abstract boolean isUnsubscribed();
    }

    /**
     * Suscriptor de Redis para un canal específico
     */
    private static class SingleChannelSubscriber extends RedisSubscriber {
        private final String channel;
        private final Consumer<String> messageHandler;
        private final Runnable onSubscribe;
        private final Runnable onUnsubscribe;

        public SingleChannelSubscriber(String channel, Consumer<String> messageHandler,
                                       Runnable onSubscribe, Runnable onUnsubscribe) {
            this.channel = channel;
            this.messageHandler = messageHandler;
            this.onSubscribe = onSubscribe;
            this.onUnsubscribe = onUnsubscribe;
        }

        @Override
        public void onMessage(String channel, String message) {
            try {
                if (messageHandler != null) {
                    messageHandler.accept(message);
                }
            } catch (Exception e) {
                logError("Error procesando mensaje de canal '" + channel + "': " + e.getMessage());
            }
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
            logInfo("Suscrito al canal: " + channel);
            if (onSubscribe != null) {
                try {
                    onSubscribe.run();
                } catch (Exception e) {
                    logError("Error en callback onSubscribe: " + e.getMessage());
                }
            }
        }

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
            logInfo("Desuscrito del canal: " + channel);
            if (onUnsubscribe != null) {
                try {
                    onUnsubscribe.run();
                } catch (Exception e) {
                    logError("Error en callback onUnsubscribe: " + e.getMessage());
                }
            }
        }

        @Override
        public boolean isUnsubscribed() {
            return false;
        }
    }

    /**
     * Suscriptor para múltiples canales
     */
    private static class MultiChannelSubscriber extends RedisSubscriber {
        private final Consumer<ChannelMessage> messageHandler;
        private final Consumer<String> onSubscribe;
        private final Consumer<String> onUnsubscribe;

        public MultiChannelSubscriber(Consumer<ChannelMessage> messageHandler,
                                      Consumer<String> onSubscribe,
                                      Consumer<String> onUnsubscribe) {
            this.messageHandler = messageHandler;
            this.onSubscribe = onSubscribe;
            this.onUnsubscribe = onUnsubscribe;
        }

        @Override
        public void onMessage(String channel, String message) {
            try {
                if (messageHandler != null) {
                    messageHandler.accept(new ChannelMessage(channel, message));
                }
            } catch (Exception e) {
                logError("Error procesando mensaje multicanal de '" + channel + "': " + e.getMessage());
            }
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
            logInfo("Suscrito al canal multicanal: " + channel);
            if (onSubscribe != null) {
                try {
                    onSubscribe.accept(channel);
                } catch (Exception e) {
                    logError("Error en callback onSubscribe multicanal: " + e.getMessage());
                }
            }
        }

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
            logInfo("Desuscrito del canal multicanal: " + channel);
            if (onUnsubscribe != null) {
                try {
                    onUnsubscribe.accept(channel);
                } catch (Exception e) {
                    logError("Error en callback onUnsubscribe multicanal: " + e.getMessage());
                }
            }
        }

        @Override
        public boolean isUnsubscribed() {
            return false;
        }
    }

    /**
     * Suscriptor por patrones
     */
    private static class PatternSubscriber extends RedisSubscriber {
        private final String pattern;
        private final Consumer<PatternMessage> messageHandler;
        private final Consumer<String> onSubscribe;
        private final Consumer<String> onUnsubscribe;

        public PatternSubscriber(String pattern, Consumer<PatternMessage> messageHandler,
                                 Consumer<String> onSubscribe, Consumer<String> onUnsubscribe) {
            this.pattern = pattern;
            this.messageHandler = messageHandler;
            this.onSubscribe = onSubscribe;
            this.onUnsubscribe = onUnsubscribe;
        }

        @Override
        public void onPMessage(String pattern, String channel, String message) {
            try {
                if (messageHandler != null) {
                    messageHandler.accept(new PatternMessage(pattern, channel, message));
                }
            } catch (Exception e) {
                logError("Error procesando mensaje de patrón '" + pattern + "': " + e.getMessage());
            }
        }

        @Override
        public void onPSubscribe(String pattern, int subscribedChannels) {
            logInfo("Suscrito al patrón: " + pattern);
            if (onSubscribe != null) {
                try {
                    onSubscribe.accept(pattern);
                } catch (Exception e) {
                    logError("Error en callback onPSubscribe: " + e.getMessage());
                }
            }
        }

        @Override
        public void onPUnsubscribe(String pattern, int subscribedChannels) {
            logInfo("Desuscrito del patrón: " + pattern);
            if (onUnsubscribe != null) {
                try {
                    onUnsubscribe.accept(pattern);
                } catch (Exception e) {
                    logError("Error en callback onPUnsubscribe: " + e.getMessage());
                }
            }
        }

        @Override
        public boolean isUnsubscribed() {
            return false;
        }
    }
}