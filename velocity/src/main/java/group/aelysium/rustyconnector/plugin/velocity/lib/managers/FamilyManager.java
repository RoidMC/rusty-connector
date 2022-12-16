package group.aelysium.rustyconnector.plugin.velocity.lib.managers;

import group.aelysium.rustyconnector.core.lib.model.NodeManager;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FamilyManager implements NodeManager<ServerFamily<? extends PaperServerLoadBalancer>> {
    private final Map<String, ServerFamily<? extends PaperServerLoadBalancer>> registeredFamilies = new HashMap<>();

    /**
     * Get a family via its name.
     * @param name The name of the family to get.
     * @return A family.
     */
    @Override
    public ServerFamily<? extends PaperServerLoadBalancer> find(String name) {
        return this.registeredFamilies.get(name);
    }

    /**
     * Add a family to this manager.
     * @param family The family to add to this manager.
     */
    @Override
    public void add(ServerFamily<? extends PaperServerLoadBalancer> family) {
        VelocityRustyConnector.getInstance().logger().log(family.toString());
        this.registeredFamilies.put(family.getName(),family);
    }

    /**
     * Remove a family from this manager.
     * @param family The family to remove from this manager.
     */
    @Override
    public void remove(ServerFamily<? extends PaperServerLoadBalancer> family) {
        this.registeredFamilies.remove(family.getName());
    }

    @Override
    public List<ServerFamily<? extends PaperServerLoadBalancer>> dump() {
        return this.registeredFamilies.values().stream().toList();
    }

    @Override
    public void clear() {
        this.registeredFamilies.clear();
    }
}
