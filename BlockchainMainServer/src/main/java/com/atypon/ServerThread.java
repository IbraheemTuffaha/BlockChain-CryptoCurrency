package com.atypon;

import java.io.*;
import java.net.Socket;

/**
 * A class that handles a single connection with a client
 * and sends it the list of clients.
 * The protocol used here is that the clients sends in the
 * IP Address and the Port separated by a line break and
 * the server responds with a string of all the clients
 * in the network each client on a line, each client
 * information is sent in the following format: 'ipAddress:port'.
 */
public class ServerThread implements Runnable {
    private Socket socket;
    private Server server;

    /**
     * Parameterized constructor to initialize the ServerThread.
     *
     * @param socket The server socket.
     * @param server The server itself.
     */
    public ServerThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
             ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {

            // Receive the message type from another client.
            Object receivedObject = in.readObject();
            System.out.println("Received an object.");

            if (receivedObject instanceof ClientSocket) {
                System.out.println("The object is a ClientSocket.");
                ClientSocket client = (ClientSocket) receivedObject;

                // Check if ipAddress is legit.
                if (!ClientSocket.isIpAddress(client.getIpAddress())) {
                    server.printOnWindow("Invalid IP Address: " + client.getIpAddress());
                    return;
                }

                // Check if port is legit.
                if (!ClientSocket.isPort(client.getPort())) {
                    server.printOnWindow("Invalid Port: " + client.getPort());
                    return;
                }

                // Add the new client to the list.
                if (server.addClient(client))
                    server.printOnWindow("Added new client '" + client.getIpAddress() + ':' + client.getPort() + "' to the list.");
                else
                    server.printOnWindow("Client with '" + client.getIpAddress() + ':' + client.getPort() + "' already exists in the list.");

                // Send back the list of clients.
                out.writeObject(server.getClients());
                out.flush();
            } else {
                // Object is not a ClientSocket
                server.printOnWindow("Data sent is not a client socket.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            server.printOnWindow("An error occurred while sending/receiving data.");
        }
    }

}
