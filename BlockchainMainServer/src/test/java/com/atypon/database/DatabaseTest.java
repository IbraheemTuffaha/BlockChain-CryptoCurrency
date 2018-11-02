package com.atypon.database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DatabaseTest {
    private final static String DATABASE_NAME = "DatabaseTestingName";
    @Before
    public void setUp() {
        // Create the database and a table.
        DatabaseUtility.createDatabase(DATABASE_NAME);
    }

    @After
    public void tearDown() {
        // Drop the database created.
        String sqlDropStatement = "DROP DATABASE " + DATABASE_NAME;
        DatabaseUtility.executeUpdate(sqlDropStatement);
    }

    @Test
    public void getReturnConnectionTest() {
        try {
            assertTrue(DatabaseUtility.returnConnection(DatabaseUtility.getConnection()));
        } catch (Exception e) {
            // getConnection failed.
            fail();
        }
    }

    @Test
    public void testConnectionTest() {
        assertTrue(DatabaseUtility.testConnection());
    }

    @Test
    public void createDatabaseAndTableTest() {
        // Test dropping the database.
        String sqlDropStatement = "DROP DATABASE " + DATABASE_NAME;
        assertTrue(DatabaseUtility.executeUpdate(sqlDropStatement) >= 0);

        // Test database and table creation.
        assertTrue(DatabaseUtility.createDatabase(DATABASE_NAME));
        assertTrue(DatabaseUtility.createTable("TableName", "id varchar(20)", ""));
    }

    @Test
    public void closeConnectionsTest() {
        // Run multiple connections then try to close them
        for(int i=0; i<200; ++i)
            testConnectionTest();

        DatabaseUtility.closeConnections();
        assertEquals(0, DatabaseUtility.getConnectionCount());
    }


}