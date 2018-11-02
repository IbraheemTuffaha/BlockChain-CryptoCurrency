package com.atypon.factory;

import com.atypon.ClientSocket;
import com.atypon.userAPI.User;

import java.io.*;
import java.security.KeyPair;

/**
 * A factory class for {@link User}.
 */
public final class UserFactory {

    /**
     * Create a user give the alias, ip address and port number.
     *
     * @param alias The user alias.
     * @param port  The user port.
     * @return An instance of a user with the given information.
     */
    public static User getInstance(String alias, String ipAddress, int port) {
        KeyPair keyPair = KeyFactory.getKeyInstance();
        ClientSocket clientSocket = ClientFactory.getSocket(ipAddress, port, alias, keyPair.getPublic());
        return new User(clientSocket, keyPair.getPrivate());
    }

    /**
     * An overloaded version that sends the IP Address as localhost.
     *
     * @param alias The user alias.
     * @param port  The user port.
     * @return An instance of a user with the given information.
     */
    public static User getInstance(String alias, int port) {
        return getInstance(alias, "127.0.0.1", port);
    }

    /**
     * Save the user data to a file.
     *
     * @param file The file name.
     * @param user The user to save.
     */
    public static void writeUser(String file, User user) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file + ".ser"))) {
            out.writeObject(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the user data from a file.
     *
     * @param file The file name.
     * @return The user read from the file, null if the user reading failed.
     */
    public static User readUser(String file) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file + ".ser"))) {
            User user = (User) in.readObject();

            if (user.getBlockchain() == null)
                return null;
            if (!user.getBlockchain().verifyChain())
                return null;

            if (user.getClientSocket() == null)
                return null;
            if (user.getClientSocket().getPublicKey() == null)
                return null;
            if (user.getClientSocket().getAlias() == null)
                return null;
            if (user.getClientSocket().getIpAddress() == null)
                return null;
            if (!(user.getClientSocket().getPort() + "").equals(file))
                return null;

            if (user.getTransactionPool() == null)
                return null;

            if (user.getClient() == null)
                return null;

            if (user.getClients() == null)
                return null;

            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * A private constructor to enforce non-instantiability.
     */
    private UserFactory() {
    }

}
