package com.atypon.database;

import com.atypon.ClientSocket;

import java.util.List;

/**
 * Database Access Object interface for ClientSocket class.
 */
public interface ClientSocketDAO {
    /**
     * Retrieve all the ClientSockets in the database.
     *
     * @return List of ClientSockets.
     */
    List<ClientSocket> findAll();

    /**
     * Creates the table 'socket' in the database if it doesn't exist.
     *
     * @return true if the table was created, false otherwise (already exists).
     */
    boolean createTable();

    /**
     * Drops the table 'socket' from the database if it exist.
     *
     * @return true if the table was dropped, false otherwise (doesn't exist).
     */
    boolean dropTable();

    /**
     * Inserts the socket into the database.
     *
     * @param socket the socket to insert.
     * @return true if the insertion succeeded, false otherwise (already exists);
     */
    boolean insertSocket(ClientSocket socket);

}
