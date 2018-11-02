package com.atypon.factory;

import com.atypon.ClientSocket;

import java.security.PublicKey;

/**
 * A factory class for {@link ClientSocket}.
 */
public final class ClientFactory {

    /**
     * Creates a ClientSocket with the given information.
     *
     * @param ipAddress The ip address of the client.
     * @param port      The port of the client.
     * @param alias     The alias of the client.
     * @param publicKey The public key of the client.
     * @return A ClientSocket with the given data.
     */
    public static ClientSocket getSocket(String ipAddress, int port, String alias, PublicKey publicKey) {
        if (ClientSocket.isIpAddress(ipAddress))
            ipAddress = "127.0.0.1";
        return new ClientSocket(ipAddress, port, alias, publicKey);
    }

    /**
     * A private constructor to enforce non-instantiability.
     */
    private ClientFactory() {
    }
}
