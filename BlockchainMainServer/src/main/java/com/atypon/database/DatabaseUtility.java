package com.atypon.database;

import java.sql.*;
import java.util.concurrent.*;

/**
 * A Utility class to simplify work with MySQL and handling
 * database connections though a database connection pool.
 */
public abstract class DatabaseUtility {

    private final static String USERNAME = "root";
    private final static String PASSWORD = "";
    private final static String HOST = "localhost";
    private final static int TRIES_LIMIT = 10;
    private final static int CONNECTION_CAPACITY = 10;
    private static String databaseName = "";
    // Uses BlockingQueue to avoid concurrency issues if multi-threading is used.
    private static BlockingQueue<Connection> connectionPool = new LinkedBlockingQueue<>(CONNECTION_CAPACITY);

    /**
     * Creates a connection with the database if the limit
     * {@link #CONNECTION_CAPACITY} isn't reached, otherwise
     * gets a connection from the connection pool.
     *
     * @return A connection.
     * @throws ClassNotFoundException If the driver fails to load.
     * @throws SQLException           If the Driver fails to connect to the database.
     * @throws InterruptedException   If the queue fails to retrieve a connection.
     */
    public static Connection getConnection() throws ClassNotFoundException, SQLException, InterruptedException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        if (connectionPool.size() < CONNECTION_CAPACITY)
            return DriverManager.getConnection("jdbc:mysql://" + HOST + "/" + databaseName, USERNAME, PASSWORD);
        else
            return connectionPool.take();
    }

    /**
     * Returns the connection to the connection pool.
     *
     * @param connection The connection to be returned.
     */
    public static boolean returnConnection(Connection connection) {
        boolean isAdded = false;
        int tries = 0;
        // While the connection isn't added back, try to add it.
        while (!isAdded) {
            try {
                connectionPool.put(connection);
                isAdded = true;

            } catch (Exception e) {
                System.out.println("Cannot add connection!");
                ++tries;
                // If the tries reach the limit, break the loop to avoid an infinite loop.
                if (tries == TRIES_LIMIT) {
                    try {
                        connection.close();
                    } catch (SQLException e1) {
                        System.out.println("Connection not added not closed!");
                    }
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Test the connection to database.
     *
     * @return true if the connection was successful, false otherwise.
     */
    public static boolean testConnection() {
        // Do a query using a built-in MySQL function.
        String sqlStatement = "SELECT version()";
        ResultSet resultSet = executeQuery(sqlStatement);
        try {
            return resultSet != null && resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if a given row exists in the table according to some specific condition.
     *
     * @param table The table to look into.
     * @param item  The name of the column.
     * @param value The value of the column.
     * @return true if the row was found, false otherwise.
     */
    private static boolean checkItem(String table, String item, String value) {
        String sqlStatement = "SELECT * FROM " + table + " WHERE " + item + "='" + value + "'";
        ResultSet resultSet = executeQuery(sqlStatement);
        try {
            return resultSet != null && resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Initialize the database name and create the database schema.
     *
     * @param databaseName Database name.
     * @return true if the creation was successful, false otherwise.
     */
    public static boolean createDatabase(String databaseName) {
        DatabaseUtility.databaseName = databaseName;
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://" + HOST, USERNAME, PASSWORD);
            connection.createStatement().executeUpdate("CREATE DATABASE " + DatabaseUtility.databaseName);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create a table in the database.
     *
     * @param tableName   The table name.
     * @param field       The columns of the table.
     * @param constraints The constrains added to the table.
     * @return true if the creation was successful, false otherwise.
     */
    public static boolean createTable(String tableName, String field, String constraints) {
        String createTable;
        if (constraints.isEmpty())
            createTable = "CREATE TABLE " + tableName + " (" + field + ");";
        else
            createTable = "CREATE TABLE " + tableName + " (" + field + "," + constraints + ");";
        return executeUpdate(createTable) >= 0;
    }

    /**
     * Execute a MySQL query.
     *
     * @param sqlStatement The SQL statement.
     * @return The ResultSet containing the result of the query.
     */
    public static ResultSet executeQuery(String sqlStatement) {
        Connection connection = null;
        try {
            connection = DatabaseUtility.getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                return statement.executeQuery(sqlStatement);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                DatabaseUtility.returnConnection(connection);
        }
        return null;
    }

    /**
     * Execute a MySQL update.
     *
     * @param sqlStatement The SQL statement.
     * @return true if the update was successful, false otherwise.
     */
    public static int executeUpdate(String sqlStatement) {
        Connection connection = null;
        try {
            connection = DatabaseUtility.getConnection();
            if (connection != null) {
                Statement statement = connection.createStatement();
                return statement.executeUpdate(sqlStatement);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
                DatabaseUtility.returnConnection(connection);
        }
        return -1;
    }

    /**
     * Close connections in the connection pool.
     */
    public static void closeConnections() {
        try {
            while (!connectionPool.isEmpty()) {
                Connection connection = connectionPool.take();
                connection.close();
            }
        } catch (Exception e) {
            connectionPool.clear();
            e.printStackTrace();
        }
    }

    /**
     * The number of connections available in the connection pool.
     *
     * @return size of the connection pool.
     */
    public static int getConnectionCount() {
        return connectionPool.size();
    }

}
