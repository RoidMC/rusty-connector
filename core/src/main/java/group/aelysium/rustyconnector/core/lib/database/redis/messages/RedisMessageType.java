package group.aelysium.rustyconnector.core.lib.database.redis.messages;

import java.util.ArrayList;
import java.util.List;

public class RedisMessageType {
    /**
     * Regarding messages, sub-servers never have any way of knowing if the Proxy is actually online or listening.
     * They simply send messages into the Redis Channel in hopes that the proxy is listening.
     * <p>
     * This is done so that we don't have the proxy's IP Address stored on the sub-servers.
     * Instead, it's the proxy's job to keep track of the IP Addresses of all sub-servers and to recognize who is speaking to it.
     * When the proxy needs to send a message to a specific server, it will include a `to` parameter which specifies which
     * sub-server is allowed to read it.
     * The sub-servers don't have a `to` parameter and instead shoot their messages into the data-channel blindly.
     * <p>
     * If a message contains the `from` parameter it is from a sub-server.
     * If a message contains the `to` parameter it is from the proxy.
     * Messages cannot contain both `to` and `from` parameters. Additionally, these parameters cannot be set manually.
     */

    public static Mapping PING = new Mapping(100, "PING");
    public static Mapping PONG = new Mapping(200, "PONG");

    /**
     * `Proxy > Server` | An outbound request for servers to register themselves
     */
    public static Mapping REGISTER_ALL_SERVERS_TO_PROXY = new Mapping(300, "REGISTER_ALL_SERVERS_TO_PROXY");

    /**
     * `Proxy > Server` | An outbound request for servers of a particular family to register themselves
     */
    public static Mapping REGISTER_ALL_SERVERS_TO_FAMILY = new Mapping(400, "REGISTER_ALL_SERVERS_TO_FAMILY");

    /**
     * `Server > Proxy` | A server's response to the REG_OUT message. This is also used when a server boots up and needs to register itself.
     */
    public static Mapping REGISTER_SERVER = new Mapping(500, "REGISTER_SERVER");

    /**
     * `Server > Proxy` | A server's message to the proxy when it needs to un-register itself.
     */
    public static Mapping UNREGISTER_SERVER = new Mapping(600, "UNREGISTER_SERVER");

    /**
     * `Server > Proxy` | Request to send a player to a family
     */
    public static Mapping SEND_PLAYER = new Mapping(700, "SEND_PLAYER");

    /**
     * `Proxy > Server` | Add a player's teleportation to the TPA queue on a specific server.
     */
    public static Mapping TPA_QUEUE_PLAYER = new Mapping(800, "TPA_QUEUE_PLAYER");

    public static List<Mapping> toList() {
        List<Mapping> list = new ArrayList<>();
        list.add(PING);
        list.add(PONG);
        list.add(REGISTER_ALL_SERVERS_TO_PROXY);
        list.add(REGISTER_ALL_SERVERS_TO_FAMILY);
        list.add(REGISTER_SERVER);
        list.add(UNREGISTER_SERVER);
        list.add(SEND_PLAYER);
        list.add(TPA_QUEUE_PLAYER);

        return list;
    }

    public static Mapping getMapping(String name) {
        return toList().stream().filter(entry -> entry.name() == name).findFirst().orElseThrow(NullPointerException::new);
    }
    public static Mapping getMapping(int id) {
        return toList().stream().filter(entry -> entry.id() == id).findFirst().orElseThrow(NullPointerException::new);
    }

    public record Mapping (Integer id, String name) {
        @Override
        public String toString() {
            return name+"-"+id;
        }
    }
}
