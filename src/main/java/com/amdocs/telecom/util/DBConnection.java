package com.amdocs.telecom.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static DBConnection instance;

    private final String url;
    private final String username;
    private final String password;

    private DBConnection() {
        Properties props = new Properties();
        try (InputStream in = DBConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties not found in src/main/resources");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read db.properties", e);
        }
        this.url = props.getProperty("db.url");
        this.username = props.getProperty("db.username");
        this.password = props.getProperty("db.password");
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}