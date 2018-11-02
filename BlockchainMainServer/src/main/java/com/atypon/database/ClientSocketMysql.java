package com.atypon.database;

import com.atypon.ClientSocket;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.sql.ResultSet;
import java.util.List;
import java.util.Vector;

/**
 * An implementation for ClientSocketDAO for MySQL,
 * for more detailed documentation: {@see ClientSocketDAO}.
 */
public class ClientSocketMysql implements ClientSocketDAO {
    /**
     * Retrieve a list of ClientSockets using for the given SQL statement.
     *
     * @param sqlStatement the SQL statement.
     * @return Vector of ClientSockets.
     */
    private List<ClientSocket> getList(String sqlStatement) {
        Vector<ClientSocket> vector = new Vector<>();
        try (ResultSet resultSet = DatabaseUtility.executeQuery(sqlStatement)) {
            if (resultSet != null)
                while (resultSet.next()) {
                    ClientSocket socket = new ClientSocket(
                            resultSet.getString("ipAddress"),
                            resultSet.getInt("port"),
                            resultSet.getString("alias"),
                            stringToPublicKey(resultSet.getString("publicKey")));
                    vector.add(socket);
                }
            if (resultSet != null)
                resultSet.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return vector;
    }

    public List<ClientSocket> findAll() {
        String sqlStatement = "SELECT * FROM socket;";
        return getList(sqlStatement);
    }

    public boolean createTable() {
        return DatabaseUtility.createTable("socket",
                "ipAddress varchar(20), port int, alias varchar(300), publicKey varchar(550)",
                "CONSTRAINT socket_pk PRIMARY KEY (ipAddress, port)");
    }

    public boolean dropTable() {
        return DatabaseUtility.executeUpdate("DROP TABLE socket") >= 0;
    }

    public boolean insertSocket(ClientSocket socket) {
        String sqlStatement = "INSERT INTO socket (ipAddress, port, alias, publicKey) VALUES ('" +
                socket.getIpAddress() + "', " + socket.getPort() + ", '" +
                socket.getAlias() + "', '" + socket.getPublicKeyString() + "');";
        return DatabaseUtility.executeUpdate(sqlStatement) > 0;
    }

    private PublicKey stringToPublicKey(String key) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(BitManipulation.stringToByteArray(key));
            return keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
