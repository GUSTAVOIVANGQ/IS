package com.version.nutrition.controller;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class Neo4jConnectionTest {
    @Autowired
    private Driver driver;

    @PostConstruct
    public void testConnection() {
        System.out.println("Testing Neo4j connection...");
        if (isConnected()) {
            System.out.println("Connection test successful!");
            printDatabaseInfo();
        } else {
            System.out.println("Connection test failed!");
        }
    }

    public boolean isConnected() {
        try (Session session = driver.session()) {
            System.out.println("Attempting to connect to Neo4j...");
            session.run("RETURN 1").single();
            return true;
        } catch (ServiceUnavailableException e) {
            System.err.println("Neo4j Database is unavailable: " + e.getMessage());
            System.err.println("Please check if:");
            System.err.println("1. Neo4j service is running");
            System.err.println("2. Connection credentials are correct");
            System.err.println("3. Database port (typically 7687) is accessible");
            return false;
        } catch (Exception e) {
            System.err.println("Error connecting to Neo4j: " + e.getMessage());
            return false;
        }
    }

    private void printDatabaseInfo() {
        try (Session session = driver.session()) {
            // Get node count
            Result countResult = session.run("MATCH (n) RETURN count(n) AS count");
            long nodeCount = countResult.single().get("count").asLong();
            
            // Get database version
            Result versionResult = session.run("CALL dbms.components() YIELD versions");
            String version = versionResult.single().get("versions").asList().get(0).toString();
            
            System.out.println("==== Neo4j Database Information ====");
            System.out.println("Database version: " + version);
            System.out.println("Total nodes: " + nodeCount);
            System.out.println("================================");
        } catch (Exception e) {
            System.err.println("Error retrieving database info: " + e.getMessage());
        }
    }
}