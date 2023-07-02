package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.database.MySQLService;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FriendsMySQLService extends MySQLService {
    private static final String FIND_FRIEND = "SELECT * FROM friends WHERE player1_uuid = ? OR player2_uuid = ?;";
    private static final String GET_FRIEND_COUNT = "SELECT COUNT(*) FROM friends WHERE player1_uuid = ? OR player2_uuid = ?;";
    private static final String DELETE_FRIEND = "DELETE FROM friends WHERE player1_uuid = ? AND player2_uuid = ?;";
    private static final String ADD_FRIEND = "INSERT INTO friends (player1_uuid, player2_uuid) VALUES(?, ?);";

    private static final String SEND_FRIEND_REQUEST = "INSERT INTO requests (sender_UUID, target_UUID) VALUES(?, ?);";
    private static final String DELETE_FRIEND_REQUEST = "DELETE FROM requests WHERE sender_UUID = ? AND target_UUID = ?;";

    private FriendsMySQLService() {
        super();
    }
    private FriendsMySQLService(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * Find all friends of a player.
     * @param player The player to find friends of.
     * @return A list of friends.
     * @throws SQLException If there was an issue.
     */
    public List<FriendMapping> findFriends(Player player) throws SQLException {
        VelocityAPI api = VelocityRustyConnector.getAPI();

        this.connect();
        PreparedStatement statement = this.prepare(FIND_FRIEND);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, player.getUniqueId().toString());

        ResultSet result = this.executeQuery(statement);

        List<FriendMapping> friends = new ArrayList<>();
        while (result.next()) {
            Player player1 = api.getServer().getPlayer(result.getString("player1_uuid")).orElse(null);
            Player player2 = api.getServer().getPlayer(result.getString("player2_uuid")).orElse(null);

            if (player1 == null) continue;
            if (player2 == null) continue;

            friends.add(new FriendMapping(player1, player2));
        }

        this.close();
        return friends;
    }

    /**
     * Get number of friends of a player.
     * @param player The player to get the friend count of.
     * @return The number of friends a player has.
     * @throws SQLException If there was an issue.
     */
    public int getFriendCount(Player player) throws SQLException {
        this.connect();
        PreparedStatement statement = this.prepare(GET_FRIEND_COUNT);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, player.getUniqueId().toString());

        ResultSet result = this.executeQuery(statement);

        int friendCount = result.getInt(0);

        this.close();
        return friendCount;
    }

    public static class Builder {
        protected boolean enabled = true;

        protected String host;
        protected int port;

        protected String database;
        protected String user;
        protected String password;

        public Builder(){}

        public FriendsMySQLService.Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public FriendsMySQLService.Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public FriendsMySQLService.Builder setDatabase(String database) {
            this.database = database;
            return this;
        }

        public FriendsMySQLService.Builder setUser(String user) {
            this.user = user;
            return this;
        }

        public FriendsMySQLService.Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public FriendsMySQLService.Builder setDisabled() {
            this.enabled = false;
            return this;
        }

        public FriendsMySQLService build(){
            if(!this.enabled) return new FriendsMySQLService();

            MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();
            dataSource.setServerName(this.host);
            dataSource.setPortNumber(this.port);

            if(this.database != null)
                dataSource.setDatabaseName(this.database);

            if(this.user != null)
                dataSource.setUser(this.user);

            if(this.password != null)
                dataSource.setPassword(this.password);

            return new FriendsMySQLService(dataSource);
        }

    }
}
