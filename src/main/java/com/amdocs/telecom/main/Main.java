package com.amdocs.telecom.main;

import com.amdocs.telecom.util.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        System.out.println("TCSMS booting up...");
        try (Connection con = DBConnection.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM telecom_plan")) {
            rs.next();
            System.out.println("Connected! Plans in DB: " + rs.getInt(1));
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }
}