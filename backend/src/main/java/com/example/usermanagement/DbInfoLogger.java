package com.example.usermanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Component
public class DbInfoLogger {
    @Autowired
    private DataSource ds;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        try (Connection c = ds.getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            System.out.println("=== DB META ===");
            System.out.println("Product: " + md.getDatabaseProductName() + " " + md.getDatabaseProductVersion());
            System.out.println("Driver: " + md.getDriverName() + " " + md.getDriverVersion());
            System.out.println("JDBC: " + md.getJDBCMajorVersion() + "." + md.getJDBCMinorVersion());
            System.out.println("AutoCommit: " + c.getAutoCommit());
            System.out.println("Isolation: " + c.getTransactionIsolation());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
