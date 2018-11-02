package com.atypon.userAPI;

import com.atypon.ClientSocket;
import com.atypon.blockchain.Block;
import com.atypon.blockchain.Blockchain;
import com.atypon.blockchain.content.MinedTransaction;
import com.atypon.blockchain.content.Transaction;
import com.atypon.factory.*;
import com.atypon.gui.Window;

import java.io.*;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * An API that uses the blockchain to create a crypto-currency where the creator of
 * the who chain gets a balance of {@link #CREATOR_STARTING_BALANCE}(Since the first
 * block is harder to create) and the mining fees are fixed to {@link #FEES_PERCENTAGE}%,
 * and the mining rewards starts with {@link #INIT_REWARD) per block and it is halved
 * every {@link #NUMBER_OF_BLOCKS_FOR_REDUCTION} blocks.
 * Has an inner class {@link Client} which handles the communication with other users.
 * Designed to be thread safe, the blocks that may cause concurrency problems were synchronized.
 */
public class User implements Serializable {
    public final static BigDecimal CREATOR_STARTING_BALANCE = BigDecimal.valueOf(500);
    public final static BigDecimal FEES_PERCENTAGE = BigDecimal.valueOf(2.0 / 100.0);
    public final static BigDecimal INIT_REWARD = BigDecimal.valueOf(50);
    public final static int NUMBER_OF_BLOCKS_FOR_REDUCTION = 5;

    // The blockchain copy held by the user.
    private final Blockchain<MinedTransaction> blockchain;
    // Holds the ip address, the port, the alias and the public key of the user.
    private final ClientSocket clientSocket;
    // The private key of the user.
    private final PrivateKey privateKey;
    // This object will handle communication with other users.
    private final Client client;
    // Hold the transactions that weren't mined and added to the blockchain yet
    private final LinkedBlockingQueue<Transaction> transactionPool;
    // If a new transaction arrive and isMiningOn is true, the user immediately mines the transaction.
    private boolean isMiningOn;
    // A reference to the Window interface to add the log to it.
    transient private Window window;

    /**
     * Parameterized constructor that initializes a user.
     *
     * @param clientSocket The clientSocket for this user.
     * @param privateKey   The private key of this user.
     */
    public User(ClientSocket clientSocket, PrivateKey privateKey) {
        this.blockchain = BlockchainFactory.getInstance();
        this.clientSocket = clientSocket;
        this.privateKey = privateKey;
        this.client = new Client();
        this.transactionPool = new LinkedBlockingQueue<>();
        this.isMiningOn = false;
        this.window = null;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// Blockchain related functions //////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    public void createChain() {
        Transaction transaction = TransactionFactory.getInstance(getPublicKey(), getPublicKey(),
                BigDecimal.valueOf(0), getPrivateKey());
        MinedTransaction minedTransaction = TransactionFactory.getMinedInstance(transaction, getPublicKey(),
                FEES_PERCENTAGE, CREATOR_STARTING_BALANCE);
        Block<MinedTransaction> block = BlockFactory.getFirstMinedInstance(minedTransaction);
        addBlock(block);
        synchronized (this.blockchain) {
            client.broadcast(blockchain);
        }
    }

    /**
     * Verifies that the blockchain is valid then replaces the existing one.
     *
     * @param blockchain The new blockchain.
     */
    public void replaceChain(Blockchain<MinedTransaction> blockchain) {
        synchronized (this.blockchain) {
            if (verifyChain(blockchain))
                this.blockchain.replaceChain(blockchain);
        }
    }

    /**
     * Add a block to the blockchain, use this instead of {@link Blockchain#addBlock}
     * since this one insures that the transaction sequence is valid.
     *
     * @param block The block to be added.
     * @return true if the block is added, false otherwise.
     */
    private boolean addBlock(Block<MinedTransaction> block) {
        synchronized (this.blockchain) {
            if (blockchain.addBlock(block)) {
                if (verifyChain(blockchain))
                    return true;
                blockchain.removeLastBlock();
            }
        }
        return false;
    }

    /**
     * Make a new transaction to a specific user.
     *
     * @param receiver The receiver public key.
     * @param amount   The amount of the transaction.
     * @return true if the transaction was made and broadcast, false otherwise.
     */
    public boolean makeTransaction(PublicKey receiver, BigDecimal amount) {
        // Insure the sender isn't the same user as the receiver.
        if (receiver.equals(getPublicKey()))
            return false;
        // Insure the user has the balance to make this transaction.
        if (getNetWorth(this.getPublicKey()).compareTo(amount) < 0)
            return false;
        // Broadcast the signed transaction so miners would mine it.
        Transaction transaction = TransactionFactory.getInstance(getPublicKey(), receiver, amount, getPrivateKey());
        client.broadcast(transaction);
        addTransaction(transaction);
        System.out.println(isMiningOn);
        if (isMiningOn())
            mine();
        return true;
    }

    /**
     * Find a valid transaction in the transaction pool, mine it and add it to the blockchain.
     *
     * @return true if the mining was successful, false otherwise.
     */
    public boolean mine() {
        Transaction transaction = null;
        // Search for a valid transaction to mine.

        synchronized (this.transactionPool) {
            while (true) {
                if (transactionPool.isEmpty())
                    break;
                try {
                    transaction = transactionPool.take();
                    // Insure the transaction is new to the blockchain and
                    // the sender has the balance to make such transaction.
                    if (notExists(transaction) &&
                            getNetWorth(transaction.getSenderPublicKey()).compareTo(transaction.getAmount()) >= 0)
                        break;
                } catch (Exception e) {
                    e.printStackTrace();
                    transactionPool.remove();
                }

                // This transaction is invalid, because the break condition was never met.
                transaction = null;
            }
        }

        // There is no transaction to be mined.
        if (transaction == null)
            return false;

        int n;
        synchronized (this.blockchain) {
            n = blockchain.length() + 1;
        }

        // Create the mined transaction with the additional mining info.
        MinedTransaction minedTransaction = TransactionFactory.getMinedInstance(transaction,
                getPublicKey(), FEES_PERCENTAGE, getReward(n));

        Block<MinedTransaction> lastBlock;
        synchronized (this.blockchain) {
            lastBlock = blockchain.lastBlock();
        }

        // Mine the block with the transaction.
        Block<MinedTransaction> block =
                BlockFactory.getMinedInstance(lastBlock, minedTransaction);

        // If the mining was interrupted then the block is null.
        if (block == null)
            return false;
        // Add the block to the blockchain, if it is successfully added then broadcast the change.
        if (addBlock(block)) {
            Blockchain<MinedTransaction> clone;
            synchronized (this.blockchain) {
                clone = blockchain.clone();
            }
            client.broadcast(clone);
            if (isMiningOn())
                mine();
            return true;
        }
        return false;
    }

    /**
     * Get the reward of mining the nth block in the chain.
     *
     * @param n The index of the block in the blockchain.
     * @return The reward for the nth block.
     */
    private BigDecimal getReward(int n) {
        BigDecimal reward = INIT_REWARD;
        while (n > NUMBER_OF_BLOCKS_FOR_REDUCTION) {
            n -= NUMBER_OF_BLOCKS_FOR_REDUCTION;
            reward = reward.multiply(BigDecimal.valueOf(0.5));
        }
        return reward;
    }

    /**
     * Add a transaction to the transaction pool.
     *
     * @param transaction The transaction to add.
     */
    public void addTransaction(Transaction transaction) {
        synchronized (this.transactionPool) {
            if (notExists(transaction) && !transactionPool.contains(transaction))
                transactionPool.add(transaction);
        }
    }

    /**
     * Checks whether a transaction exists in the blockchain or not.
     *
     * @param transaction The transaction to check.
     * @return true if the transaction doesn't exist in the blockchain, false otherwise.
     */
    private boolean notExists(Transaction transaction) {
        if (transaction == null)
            return false;
        String id = transaction.getId();
        synchronized (this.blockchain) {
            for (Block<MinedTransaction> block : blockchain.getBlocks()) {
                if (block.getData().getId().equals(id))
                    return false;
            }
        }
        return true;
    }

    /**
     * Adds another layer of verification over the {@link Blockchain#verifyChain}
     * method since that one doesn't verify that the sequence of transactions are
     * valid, like make sure all users have positive balance and the mining fees and
     * rewards are correct and that the sender isn't the same person as the receiver.
     *
     * @return true if the blockchain is valid, false otherwise.
     */
    public static boolean verifyChain(Blockchain<MinedTransaction> blockchain) {
        // Make sure all balances are positive.
        HashMap<PublicKey, BigDecimal> accounts = getAllNetWorth(blockchain);
        for (Map.Entry<PublicKey, BigDecimal> account : accounts.entrySet()) {

            if (account.getValue().compareTo(BigDecimal.ZERO) < 0)
                return false;
        }

        MinedTransaction transaction;
        BigDecimal miningReward = INIT_REWARD;
        int blockCounter = 0;
        for (Block<MinedTransaction> block : blockchain.getBlocks()) {
            transaction = block.getData();
            // Check mining fee
            if (transaction.getMiningFee().compareTo(transaction.getAmount().multiply(FEES_PERCENTAGE)) != 0)
                return false;
            // Check mining reward
            ++blockCounter;

            if (blockCounter == 1) { // If first block.
                // Make sure the reward is equal to the starting balance for the creator.
                if (transaction.getMiningReward().compareTo(CREATOR_STARTING_BALANCE) != 0)
                    return false;

            } else { // If any other block.
                // Make sure the mining reward is correct.
                if (transaction.getMiningReward().compareTo(miningReward) != 0)
                    return false;

                // make sure the sender isn't the same as the receiver
                if (block.getData().getSenderPublicKey().equals(block.getData().getReceiverPublicKey()))
                    return false;
            }

            if (blockCounter % NUMBER_OF_BLOCKS_FOR_REDUCTION == 0)
                miningReward = miningReward.multiply(BigDecimal.valueOf(0.5));

        }

        // Make sure the chain is correct
        return blockchain.verifyChain();
    }

    /**
     * Calculate the net worth of all the users.
     *
     * @return A hash map containing the net worth of all the users.
     */
    private static HashMap<PublicKey, BigDecimal> getAllNetWorth(Blockchain<MinedTransaction> blockchain) {

        HashMap<PublicKey, BigDecimal> accounts = new HashMap<>();
        for (Block<MinedTransaction> block : blockchain.getBlocks()) {

            MinedTransaction transaction = block.getData();

            // Get & change sender account.
            PublicKey senderPublicKey = transaction.getSenderPublicKey();
            BigDecimal senderNetWorth = accounts.get(senderPublicKey);
            if (senderNetWorth == null) senderNetWorth = BigDecimal.ZERO;
            accounts.put(senderPublicKey, senderNetWorth.subtract(transaction.getAmount()));

            // Get & change receiver account.
            PublicKey receiverPublicKey = transaction.getReceiverPublicKey();
            BigDecimal receiverNetWorth = accounts.get(receiverPublicKey);
            if (receiverNetWorth == null) receiverNetWorth = BigDecimal.ZERO;
            accounts.put(receiverPublicKey, receiverNetWorth.add(transaction.getAmount()
                    .subtract(transaction.getMiningFee())));

            // Get & change miner account.
            PublicKey minerPublicKey = transaction.getMinerPublicKey();
            BigDecimal minerNetWorth = accounts.get(minerPublicKey);
            if (minerNetWorth == null) minerNetWorth = BigDecimal.ZERO;
            accounts.put(minerPublicKey, minerNetWorth.add(transaction.getMiningFee()
                    .add(transaction.getMiningReward())));

        }
        return accounts;
    }

    /**
     * Calculate the net worth of a single user.
     *
     * @param account The PublicKey of the user's account.
     * @return AThe net worth the user.
     */
    public BigDecimal getNetWorth(PublicKey account) {
        BigDecimal netWorth = BigDecimal.ZERO;
        Blockchain<MinedTransaction> blockchain;
        synchronized (this.blockchain) {
            blockchain = this.blockchain.clone();
        }

        for (Block<MinedTransaction> block : blockchain.getBlocks()) {
            MinedTransaction transaction = block.getData();

            if (transaction.getSenderPublicKey().equals(account))
                netWorth = netWorth.subtract(transaction.getAmount());

            if (transaction.getReceiverPublicKey().equals(account))
                netWorth = netWorth.add(transaction.getAmount().subtract(transaction.getMiningFee()));

            if (transaction.getMinerPublicKey().equals(account))
                netWorth = netWorth.add(transaction.getMiningFee().add(transaction.getMiningReward()));
        }
        return netWorth;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// Setters and Getters ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    public Blockchain<MinedTransaction> getBlockchain() {
        return blockchain;
    }

    public String getAlias() {
        return clientSocket.getAlias();
    }

    /**
     * Get the Alias of the user with a specific public key.
     *
     * @param publicKey The user's public key.
     * @return The alias of the public key.
     */
    public String getAlias(PublicKey publicKey) {
        for (ClientSocket clientSocket : getClients()) {
            if (clientSocket.getPublicKey().equals(publicKey))
                return clientSocket.getAlias();
        }
        return "";
    }

    public Vector<ClientSocket> getClients() {
        synchronized (this.client) {
            return client.clients;
        }
    }

    public String getIpAddress() {
        return clientSocket.getIpAddress();
    }

    public int getPort() {
        return clientSocket.getPort();
    }

    public PublicKey getPublicKey() {
        return clientSocket.getPublicKey();
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public ClientSocket getClientSocket() {
        return clientSocket;
    }

    public Client getClient() {
        synchronized (this.client) {
            return client;
        }
    }

    public LinkedBlockingQueue<Transaction> getTransactionPool() {
        synchronized (transactionPool) {
            return transactionPool;
        }
    }

    /**
     * Checks if the user mining is on.
     *
     * @return true if the user is currently a miner, false otherwise.
     */
    public boolean isMiningOn() {
        return isMiningOn;
    }

    /**
     * Changes the state if the miner, true to set the user to work as a miner
     * and false to stop the user from mining.
     *
     * @param miningOn The state of the miner.
     */
    public void setMiningOn(boolean miningOn) {
        if (!miningOn)
            BlockFactory.stop();
        isMiningOn = miningOn;
    }

    /**
     * Run the user to listen to other users.
     */
    public void runListener() {
        synchronized (this.client) {
            client.start();
        }
    }

    /**
     * Stop listening to other users, terminates the threads.
     */
    public void stopListener() {
        synchronized (this.client) {
            client.stopRunning();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// Overridden 'Object' methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "User{\n" +
                "blockchain         :" + blockchain + '\n' +
                "clientSocket       :" + clientSocket + '\n' +
                "privateKey         :" + privateKey + '\n' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getBlockchain(), user.getBlockchain()) &&
                Objects.equals(clientSocket, user.clientSocket) &&
                Objects.equals(getPrivateKey(), user.getPrivateKey()) &&
                Objects.equals(getClient(), user.getClient()) &&
                Objects.equals(getTransactionPool(), user.getTransactionPool());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBlockchain(), clientSocket, getPrivateKey(), getClient(),
                getTransactionPool());
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// Window related functions //////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    public void setWindow(Window window) {
        this.window = window;
    }

    public void printOnWindow(String message) {
        System.out.println(message);
        if (window != null)
            window.updateLog(message);
    }

    ////////////////////////////////////////////////////////////////////////////////
    //////////////////// The inner class Client ////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * This inner class will handle the communication between the users.
     * It holds a list of all other clients.
     * The class extends {@link Thread} so it runs a socket to
     * listen for incoming data from other users.
     */
    public class Client extends Thread implements Serializable {
        // Constants regarding the threading.
        private final static int NUMBER_OF_THREADS = 20;
        private final static int SHUTDOWN_WAITING_TIME = 3;
        // Information about the server.
        private final static String SERVER_IP_ADDRESS = "127.0.0.1";
        private final static int SERVER_PORT = 2000;
        // Information of the other clients in the network.
        private final Vector<ClientSocket> clients;
        // A global reference to stop the listener
        transient private ServerSocket serverSocket;

        /**
         * A default constructor that initialize Client object
         * by setting the user reference and connecting to the
         * server to get the list of all other users on the network.
         */
        public Client() {
            clients = new Vector<>();
            addClient(clientSocket);
        }

        /**
         * Connects to the server and gets a list of all users on the network.
         */
        public boolean getClientsFromServer() {
            // Get the clients from the server, the server ignores the message.
            Vector<Object> clientObjects = sendMessage(
                    ClientFactory.getSocket(SERVER_IP_ADDRESS, SERVER_PORT, null, null),
                    null);
            if (clientObjects == null)
                return false;
            // Add the clients to the clients vector.
            for (Object object : clientObjects)
                addClient(object);
            return true;
        }


        /**
         * Override {@link Thread#run} to run a multi-threaded server
         * that listens to other users and receives data from them.
         */
        @Override
        public void run() {
            ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
            try (ServerSocket serverSocketLocal = new ServerSocket(getPort())) {
                serverSocket = serverSocketLocal;
                while (true) {
                    printOnWindow("Client is listening on port: " + getPort() + ".");
                    // Listen on the socket in a new thread
                    executor.submit(new ClientThread(serverSocketLocal.accept(), User.this));
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
         * Terminate the thread running on the client listener.
         */
        public void stopRunning() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Broadcasts an object to all other nodes on the network.
         *
         * @param message The object to send.
         */
        private void broadcast(Serializable message) {

            Vector<ClientSocket> clone;
            synchronized (clients) {
                clone = new Vector<>(clients);
            }
            for (ClientSocket client : clone) {
                sendMessage(client, message);
            }
        }

        /**
         * Send an object to a specific node on the network.
         *
         * @param receiver The receiving node.
         * @param message  The object to send.
         * @return A vector of objects which is the response of the server.
         */
        private Vector<Object> sendMessage(ClientSocket receiver, Serializable message) {
            if (receiver.getIpAddress().equals(getIpAddress()) && receiver.getPort() == getPort())
                return new Vector<>();
            try (Socket socket = new Socket(receiver.getIpAddress(), receiver.getPort());
                 ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {

                // Send the clientSocket so the receiver save it.
                out.writeObject(clientSocket);
                // Send message.
                out.writeObject(message);
                out.flush();
                // Receive the response.
                try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()))) {
                    // Receive response.
                    return (Vector<Object>) in.readObject();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Add a client to the list of clients.
         *
         * @param object The client to add (referenced as {@link Object}).
         */
        public void addClient(Object object) {
            try {
                if (object != null) {
                    ClientSocket client = (ClientSocket) object;
                    synchronized (clients) {
                        // Insure that the client doesn't already exist.
                        for (ClientSocket thatClient : clients) {
                            if (client.getIpAddress().equals(thatClient.getIpAddress())
                                    && client.getPort() == thatClient.getPort())
                                return;
                        }
                        clients.add(client);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
