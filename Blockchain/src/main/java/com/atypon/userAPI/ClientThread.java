package com.atypon.userAPI;

import com.atypon.blockchain.Blockchain;
import com.atypon.blockchain.content.MinedTransaction;
import com.atypon.blockchain.content.Transaction;

import java.io.*;
import java.net.Socket;
import java.util.Vector;

/**
 * A class that handles an incoming connection.
 * Receives the message and responds to it.
 */
public class ClientThread implements Runnable {
    private final Socket socket;
    private final User user;

    public ClientThread(Socket socket, User user) {
        this.socket = socket;
        this.user = user;
    }

    /**
     * Override {@link Runnable#run} to run the connection in a separate thread.
     */
    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
             ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()))) {

            // Receive the client socket of the sender.
            Object clientSocketObject = in.readObject();
            // Add the client socket to the clients list.
            user.getClient().addClient(clientSocketObject);

            // Receive the message from the sender
            Object receivedObject = in.readObject();

            System.out.println("Received an object.");

            if (receivedObject instanceof Blockchain) {
                user.printOnWindow("Received a blockchain.");
                user.replaceChain((Blockchain<MinedTransaction>) receivedObject);
            } else if (receivedObject instanceof Transaction) {
                user.printOnWindow("The object is a Transaction.");
                user.addTransaction((Transaction) receivedObject);
            }
            // No response, send empty Vector.
            out.writeObject(new Vector<>());
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // After receiving an item, if mining is on, go mine.
        if (user.isMiningOn())
            user.mine();
    }

}

