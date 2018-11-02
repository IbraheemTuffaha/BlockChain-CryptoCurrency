package com.atypon.blockchain.content;

import java.io.Serializable;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Objects;

import com.atypon.blockchain.Blockable;
import com.atypon.utility.BitManipulation;
import com.atypon.utility.Hash;

/**
 * Holds the transaction information, the sender, the receiver,
 * the id, the amount and the signature.
 * Immutable thus threadsafe.
 */
public class Transaction implements Blockable, Serializable {

    private final String id;
    private final PublicKey senderPublicKey, receiverPublicKey;
    private final BigDecimal amount;
    private final String signature;

    /**
     * Initialize the transaction.
     *
     * @param id                the ID.
     * @param senderPublicKey   The sender Public Key.
     * @param receiverPublicKey The receiver Public Key.
     * @param amount            The amount transferred.
     * @param signature         The signature (by the sender private key) for the Transaction.
     */
    public Transaction(String id, PublicKey senderPublicKey, PublicKey receiverPublicKey, BigDecimal amount,
                       String signature) {
        this.id = id;
        this.senderPublicKey = senderPublicKey;
        this.receiverPublicKey = receiverPublicKey;
        this.amount = amount;
        this.signature = signature;
    }

    /**
     * Initialize and sign the transaction.
     *
     * @param id                the ID.
     * @param senderPublicKey   The sender Public Key.
     * @param receiverPublicKey The receiver Public Key.
     * @param amount            The amount transferred.
     * @param senderPrivateKey  The sender Private Key to sign the Transaction.
     */
    public Transaction(String id, PublicKey senderPublicKey, PublicKey receiverPublicKey, BigDecimal amount,
                       PrivateKey senderPrivateKey) {
        this.id = id;
        this.senderPublicKey = senderPublicKey;
        this.receiverPublicKey = receiverPublicKey;
        this.amount = amount;
        this.signature = generateSignature(senderPrivateKey);
    }

    /**
     * Hashes the data of the transaction.
     *
     * @return The hash of the data.
     */
    @Override
    public String hash() {
        return getHash();
    }

    /**
     * Because the hash() method will be Overridden,
     * this method will keep giving the parent hashing.
     *
     * @return The hash of the data.
     */
    protected String getHash() {
        return Hash.hash(id, getSenderPublicKeyString(), getReceiverPublicKeyString(), amount.toString());
    }

    /**
     * Given a private key, the function finds the signature
     * for the transactions using SHA1WithRSA algorithm.
     *
     * @param privateKey Used to sign the transaction.
     * @return A string representing the signature,
     * or an empty string if an error occurs.
     */
    private String generateSignature(PrivateKey privateKey) {
        // Get the hash string that represents the data and convert to byte array
        byte[] data = BitManipulation.stringToByteArray(this.getHash());
        try {
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initSign(privateKey);
            signature.update(data);
            return BitManipulation.byteArrayToString(signature.sign());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Verifies that the data were signed with the private key
     * associated with the sender's public key.
     *
     * @return true if the signature is correct, false otherwise.
     */
    public boolean verifySignature() {
        // Get the hash string that represents the data and convert to byte array
        byte[] data = BitManipulation.stringToByteArray(this.getHash());
        try {
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initVerify(senderPublicKey);
            signature.update(data);
            return signature.verify(BitManipulation.stringToByteArray(this.signature));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// Setters and Getters ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Gets the actual value of the Key as a hexadecimal String.
     *
     * @return the sender public key.
     */
    private String getSenderPublicKeyString() {
        return BitManipulation.byteArrayToString(senderPublicKey.getEncoded());
    }

    /**
     * Gets the actual value of the Key as a hexadecimal String.
     *
     * @return the receiver public key.
     */
    private String getReceiverPublicKeyString() {
        return BitManipulation.byteArrayToString(receiverPublicKey.getEncoded());
    }

    @Override
    public String getId() {
        return id;
    }

    public PublicKey getSenderPublicKey() {
        return senderPublicKey;
    }

    public PublicKey getReceiverPublicKey() {
        return receiverPublicKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getSignature() {
        return signature;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// Overridden 'Object' methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getSenderPublicKey(), that.getSenderPublicKey()) &&
                Objects.equals(getReceiverPublicKey(), that.getReceiverPublicKey()) &&
                Objects.equals(getAmount(), that.getAmount()) &&
                Objects.equals(getSignature(), that.getSignature());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getSenderPublicKey(), getReceiverPublicKey(), getAmount(), getSignature());
    }

    @Override
    public String toString() {
        return "\n\tTransaction{\n" +
                "\t\tid                :" + id + '\n' +
                "\t\tsenderPublicKey   :" + getSenderPublicKeyString() + '\n' +
                "\t\treceiverPublicKey :" + getReceiverPublicKeyString() + '\n' +
                "\t\tamount            :" + amount + '\n' +
                "\t\tsignature         :" + signature + '\n' +
                "\t}";
    }
}
