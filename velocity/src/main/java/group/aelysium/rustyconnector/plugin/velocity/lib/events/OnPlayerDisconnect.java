package group.aelysium.rustyconnector.plugin.velocity.lib.events;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;
import net.kyori.adventure.text.Component;

public class OnPlayerDisconnect {
    /**
     * Runs when a player disconnects from the proxy
     * This event prevents Velocity from attempting to connect the player to a velocity.toml server upon disconnect.
     */
    @Subscribe(order = PostOrder.LAST)
    public EventTask onPlayerDisconnect(DisconnectEvent event) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        return EventTask.async(() -> {
            try {
                Player player = event.getPlayer();
                if(player == null) return;

                if(player.getCurrentServer().isPresent()) {
                    PaperServer server = plugin.getProxy().findServer(player.getCurrentServer().get().getServerInfo());
                    server.playerLeft();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}