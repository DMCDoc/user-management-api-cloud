package com.example.usermanagement;

import java.sql.Driver;
import java.util.ServiceLoader;

public class JDBCTest {
    public static void main(String[] args) {
        System.out.println("=== Test de chargement du driver JDBC ===");

        ServiceLoader<Driver> drivers = ServiceLoader.load(Driver.class);
        for (Driver driver : drivers) {
            System.out.println("Driver trouvé: " + driver.getClass().getName());
        }

        try {
            Class.forName(System.getenv("SPRING_DATASOURCE_DRIVER_CLASS_NAME"));
            System.out.println("Driver chargé avec succès!");
        } catch (ClassNotFoundException e) {
            System.err.println("Échec du chargement du driver!");
            e.printStackTrace();
        }
    }
}