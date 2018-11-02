package com.atypon;

import com.atypon.utility.BitManipulation;
import java.io.Serializable;
import java.util.Objects;
import java.security.PublicKey;

/**
 * Simple class that holds information about the users in the peer2peer
 * network, such as their IP address and Port number as Strings.
 * Also contains the Alias and the PublicKey to identify the receiver.
 * Contains a parameterized constructor and setters and getters.
 * The class is Immutable, so it's threadsafe.
 */
public final class ClientSocket implements Serializable {
    private final String ipAddress;
    private final int port;
    private final String alias;
    private final PublicKey publicKey;

    /**
     * A parameterized constructor to initialize ClientSocket.
     *
     * @param ipAddress Socket IP Address.
     * @param port      Socket port.
     * @param alias     The user alias.
     * @param publicKey The user public key.
     */
    public ClientSocket(String ipAddress, int port, String alias, PublicKey publicKey) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.alias = alias;
        this.publicKey = publicKey;
    }

    /**
     * Checks whether a given string is an IPv4 Address
     *
     * @param address the string to check.
     * @return true of the given address is an IPv4 address, false otherwise.
     */
    public static boolean isIpAddress(String address) {
        if (address == null || address.isEmpty())
            return false;
        String[] parts = address.split("\\.");
        if (parts.length != 4)
            return false;
        try {
            for (String s : parts) {
                int i = Integer.parseInt(s);
                if (i < 0 || i > 255)
                    return false;
            }

            return !address.endsWith(".");
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks whether a given port is a valid port number.
     *
     * @param port the port to check.
     * @return true of the given port is a valid port number, false otherwise.
     */
    public static boolean isPort(int port) {
        return port >= 0 && port < (1 << 16);
    }

    /**
     * Overridden version of the function {@link #isPort} which takes a string.
     * @param port the port to check.
     * @return true of the given port is a valid port number, false otherwise.
     */
    public static boolean isPort(String port) {
        try {
            return isPort(Integer.parseInt(port));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// Setters and Getters ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public String getAlias() {
        return alias;
    }

    public String getPublicKeyString() {
        return BitManipulation.byteArrayToString(publicKey.getEncoded());
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// Overridden 'Object' methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return ipAddress + ':' + port + ':' + alias + ':' + getPublicKeyString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientSocket)) return false;
        ClientSocket that = (ClientSocket) o;
        return getPort() == that.getPort() &&
                Objects.equals(getIpAddress(), that.getIpAddress()) &&
                Objects.equals(getAlias(), that.getAlias()) &&
                Objects.equals(getPublicKey(), that.getPublicKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIpAddress(), getPort(), getAlias(), getPublicKey());
    }
}
