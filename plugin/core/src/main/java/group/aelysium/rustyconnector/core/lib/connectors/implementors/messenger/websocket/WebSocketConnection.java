package group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.hash.AESCryptor;
import group.aelysium.rustyconnector.core.lib.hash.MD5;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebSocketConnection extends MessengerConnection {
    private final Vector<Session> subscribers = new Vector<>();
    ExecutorService executorService;
    private Optional<AESCryptor> connectCryptor = Optional.empty();
    private final AESCryptor packetCryptor;
    private final WebSocketContainer container;
    private final URI uri;

    public WebSocketConnection(PacketOrigin origin, URI uri, AESCryptor connectCryptor, AESCryptor packetCryptor) throws IllegalArgumentException {
        super(origin);
        this.container = ContainerProvider.getWebSocketContainer();
        this.uri = uri;
        if(connectCryptor != null) this.connectCryptor = Optional.of(connectCryptor);
        this.packetCryptor = packetCryptor;
    }

    protected ClientEndpointConfig config() {
        Map<String, List<String>> headers = new HashMap<>();

        {
            List<String> authentication = new ArrayList<>();

            JsonObject object = new JsonObject();
            object.add("hash", new JsonPrimitive(MD5.generateMD5()));
            object.add("time", new JsonPrimitive(Instant.now().getEpochSecond()));
            String payload = object.toString();

            try {
                if (this.connectCryptor.isPresent()) payload = connectCryptor.get().encrypt(payload);
            } catch (Exception e) {
                e.printStackTrace();
            }

            authentication.add(payload);

            headers.put("Authentication", authentication);
        }

        {
            List<String> origin = new ArrayList<>();
            origin.add("PROXY");
            headers.put("RC-Origin", origin);
        }

        ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator();
        configurator.beforeRequest(headers);

        return ClientEndpointConfig.Builder.create().configurator(configurator).build();
    }

    @Override
    protected void subscribe(MessageCacheService cache, PluginLogger logger, Map<PacketType.Mapping, PacketHandler> handlers) {
        this.executorService.submit(() -> {
            try {
                WebSocketSubscriber websocket = new WebSocketSubscriber(this.packetCryptor, cache, logger, handlers, this.origin);

                try(Session session = WebSocketConnection.this.container.connectToServer(websocket.listener(), this.config(), uri)) {
                    session.addMessageHandler(websocket.handler());
                    this.subscribers.add(session);

                    websocket.listener().awaitClose();

                    if(session.isOpen()) session.close();
                    this.subscribers.remove(session);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                WebSocketConnection.this.subscribe(cache, logger, handlers);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void startListening(MessageCacheService cache, PluginLogger logger, Map<PacketType.Mapping, PacketHandler> handlers) {
        this.executorService = Executors.newFixedThreadPool(3);
        this.subscribe(cache, logger, handlers);
    }

    @Override
    public void kill() {
        this.subscribers.forEach(session -> {
            try {
                session.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            this.executorService.shutdown();
            try {
                if (!this.executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    this.executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                this.executorService.shutdownNow();
            }
        } catch (Exception ignore) {}
    }

    @Override
    public void publish(GenericPacket message) {
        if(!message.sendable()) throw new IllegalStateException("Attempted to send a Message that isn't sendable!");

        String signedPacket;
        try {
            signedPacket = this.packetCryptor.encrypt(message.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try(Session session = WebSocketConnection.this.container.connectToServer(new WebSocketListener(), uri)) {
            session.getBasicRemote().sendText(signedPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
