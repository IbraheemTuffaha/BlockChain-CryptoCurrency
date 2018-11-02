package com.atypon;


import com.atypon.database.ClientSocketMysql;
import com.atypon.database.DatabaseUtility;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Vector;
import java.util.concurrent.*;

/**
 * Server class which contains the main functionality of the server.
 * The class extends Thread as it runs independently from the
 * interface thread from Window class.
 */
public class Server extends Thread {
    private final static int NUMBER_OF_THREADS = 15;
    private final static int SHUTDOWN_WAITING_TIME = 3;
    private final static String DATABASE_NAME = "blockchain_main_server";

    // A reference to the running server socket to be able to interrupt it.
    private ServerSocket serverSocket;
    // A boolean to check if the server is running or not.
    private boolean isRunning;
    // The port to run the server on.
    private final int port;
    // A reference to the Window interface to add the log to it.
    private final Window window;
    // ConcurrentHashMap to save clients as it is thread safe.
    private Vector<ClientSocket> clients;
    // the DAO object used to connect to database.
    private final ClientSocketMysql clientSocketDAO;

    /**
     * Parameterized constructor to initialize a server.
     *
     * @param port   The port to use for the server.
     * @param window The window calling the server (to return the log back).
     */
    public Server(int port, Window window) {
        this.isRunning = true;
        this.port = port;
        this.window = window;
        this.clients = new Vector<>();
        this.clientSocketDAO = new ClientSocketMysql();
    }

    /**
     * The method called when the server starts as a separate thread.
     */
    @Override
    public void run() {
        establishDatabaseConnection();
        runServerSocket();
    }

    /**
     * Establishes connection to database.
     */
    private void establishDatabaseConnection() {
        // Test connection to database
        if (DatabaseUtility.testConnection())
            this.printOnWindow("Connected to database.");
        else {
            this.printOnWindow("Unable to connect to database.");
            return;
        }
        // Create database if not exists
        if (DatabaseUtility.createDatabase(DATABASE_NAME))
            this.printOnWindow("Database '" + DATABASE_NAME + "' created.");

        // Create table if not exists
        if (clientSocketDAO.createTable())
            this.printOnWindow("Table 'socket' created.");

        // Read all clients from database to the list
        this.clients = (Vector<ClientSocket>) clientSocketDAO.findAll();
    }

    /**
     * Listens to a connection, once listened, it answers
     * the connection in another thread.
     */
    private void runServerSocket() {
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        try (ServerSocket serverSocketLocal = new ServerSocket(port)) {
            // Save the serverSocket globally to terminate the loop from outside
            serverSocket = serverSocketLocal;
            while (isRunning) {
                this.printOnWindow("Server is listening.");
                // Listen on the socket in a new thread
                executor.submit(new ServerThread(serverSocketLocal.accept(), this));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Shutdown all threads
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_WAITING_TIME, TimeUnit.SECONDS))
                executor.shutdownNow();
        } catch (Exception e) {
            executor.shutdownNow();
        }
    }

    /**
     * Stops the server from running by setting the isRunning flag used
     * by the while loop to false and also closing the ServerSocket which
     * interrupts the listening for a new connection thus breaking loop.
     */
    public void stopServerSocket() {
        try {
            boolean wasRunning = isRunning;
            serverSocket.close();
            DatabaseUtility.closeConnections();
            isRunning = false;
            if (wasRunning) {
                this.printOnWindow("Database connection closed.");
                this.printOnWindow("Server has stopped.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a client to the list of clients and to the database.
     *
     * @param client The client to add.
     * @return false if the item already exists, true otherwise.
     */
    public boolean addClient(ClientSocket client) {
        if (!clientSocketDAO.insertSocket(client))
            return false;
        clients.add(client);
        return true;
    }

    /**
     * Delete and re-create the table to reset it.
     */
    public void resetDatabase() {
        clientSocketDAO.dropTable();
        clientSocketDAO.createTable();
        clients.clear();
    }

    /**
     * Check if the server is running.
     *
     * @return True if the server is running, false otherwise.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Print the log on standard output as well as the interface log.
     *
     * @param message The log message to be printed.
     */
    public void printOnWindow(String message) {
        window.updateLog(message + '\n');
        System.out.println(message + '\n');
    }

    /**
     * Return the client sockets.
     *
     * @return The vector of client sockets.
     */
    public Vector<ClientSocket> getClients() {
        return clients;
    }
}
