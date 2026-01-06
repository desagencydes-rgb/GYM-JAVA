package com.gym.app.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Helper class for managing SQLite database connections and schema
 * initialization.
 */
public class DatabaseHelper {

        // The connection string for the SQLite database. creates 'gym.db' in the
        // project root.
        private static final String URL = "jdbc:sqlite:gym.db";

        /**
         * Establishes a connection to the SQLite database.
         * 
         * @return A Connection object if successful, null otherwise.
         */
        public static Connection connect() {
                Connection conn = null;
                try {
                        // Attempt to create a connection using the DriverManager
                        conn = DriverManager.getConnection(URL);
                } catch (SQLException e) {
                        // Print any connection errors to the console
                        System.out.println(e.getMessage());
                }
                return conn;
        }

        /**
         * Initializes the database by creating necessary tables if they do not exist.
         * Also handles simple schema migrations (like adding new columns).
         */
        public static void initializeDatabase() {
                // SQL statements for table creation

                // Table for application users (Admins)
                String sqlUsers = "CREATE TABLE IF NOT EXISTS users (\n"
                                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" // Unique ID
                                + " username TEXT NOT NULL UNIQUE,\n" // Login username
                                + " password TEXT NOT NULL,\n" // Login password
                                + " role TEXT NOT NULL\n" // Role (e.g., ADMIN)
                                + ");";

                // Table for Gym Members
                String sqlMembers = "CREATE TABLE IF NOT EXISTS members (\n"
                                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n" // Unique ID
                                + " first_name TEXT NOT NULL,\n" // First Name
                                + " last_name TEXT NOT NULL,\n" // Last Name
                                + " phone TEXT NOT NULL,\n" // Phone Number (validated as 10 digit int in app)
                                + " email TEXT NOT NULL,\n" // Email Address (validated for @domain in app)
                                + " gender TEXT,\n" // Gender (Male/Female)
                                + " photo_path TEXT,\n" // Path to local photo file (legacy)
                                + " registration_date TEXT NOT NULL,\n" // Date of registration (ISO format)
                                + " face_id TEXT\n" // Token from Facial Recognition API
                                + ");";

                // Table for Coaches/Trainers
                String sqlCoaches = "CREATE TABLE IF NOT EXISTS coaches (\n"
                                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                                + " name TEXT NOT NULL,\n" // Coach Name
                                + " specialization TEXT,\n" // Area of expertise
                                + " phone TEXT\n" // Contact number
                                + ");";

                // Table for Membership Subscriptions
                String sqlSubscriptions = "CREATE TABLE IF NOT EXISTS subscriptions (\n"
                                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                                + " member_id INTEGER NOT NULL,\n" // FK link to members table
                                + " plan_name TEXT NOT NULL,\n" // Name of the plan (e.g., Monthly)
                                + " start_date TEXT NOT NULL,\n" // Start Date
                                + " end_date TEXT NOT NULL,\n" // End Date (Expiry)
                                + " price REAL NOT NULL,\n" // Cost of plan
                                + " status TEXT,\n" // Active, Expired
                                + " FOREIGN KEY (member_id) REFERENCES members (id)\n"
                                + ");";

                // Table for Payment Records
                String sqlPayments = "CREATE TABLE IF NOT EXISTS payments (\n"
                                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                                + " member_id INTEGER NOT NULL,\n" // FK link to members
                                + " amount REAL NOT NULL,\n" // Amount paid
                                + " payment_date TEXT NOT NULL,\n" // Date of payment
                                + " method TEXT NOT NULL,\n" // Cash, TPE, etc.
                                + " FOREIGN KEY (member_id) REFERENCES members (id)\n"
                                + ");";

                // Table for Attendance Records
                String sqlAttendance = "CREATE TABLE IF NOT EXISTS attendance (\n"
                                + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                                + " member_id INTEGER NOT NULL,\n" // FK link to members
                                + " check_in_time TEXT NOT NULL,\n" // Timestamp of arrival
                                + " check_out_time TEXT,\n" // Timestamp of departure (optional)
                                + " date TEXT NOT NULL,\n" // Date of attendance
                                + " FOREIGN KEY (member_id) REFERENCES members (id)\n"
                                + ");";

                // Admin account insertion if not exists
                String sqlAdmin = "INSERT OR IGNORE INTO users (id, username, password, role) VALUES (1, 'admin', 'admin123', 'ADMIN');";

                // Execute construction logic
                try (Connection conn = connect();
                                Statement stmt = conn.createStatement()) {

                        // Execute creation statements in order
                        stmt.execute(sqlUsers);
                        stmt.execute(sqlMembers);
                        stmt.execute(sqlCoaches);
                        stmt.execute(sqlSubscriptions);
                        stmt.execute(sqlPayments);
                        // stmt.execute(sqlPayments); // Duplicate execution in original code
                        // removed/ignored safely
                        stmt.execute(sqlAttendance);

                        // Table for Schedules (Classes/Sessions)
                        String sqlSchedules = "CREATE TABLE IF NOT EXISTS schedules (\n"
                                        + " id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                                        + " coach_id INTEGER NOT NULL,\n" // FK link to coaches
                                        + " day TEXT NOT NULL,\n" // Day of week
                                        + " start_time TEXT NOT NULL,\n" // HH:mm
                                        + " end_time TEXT NOT NULL,\n" // HH:mm
                                        + " title TEXT,\n" // Class name
                                        + " FOREIGN KEY (coach_id) REFERENCES coaches (id) ON DELETE CASCADE\n"
                                        + ");";
                        stmt.execute(sqlSchedules);

                        // Insert default admin user if the table was empty/new
                        stmt.execute(sqlAdmin);

                        System.out.println("Database initialized.");

                        // Migration: Add face_id column if not exists (for updates from older versions)
                        try (Statement migrationStmt = conn.createStatement()) {
                                migrationStmt.execute("ALTER TABLE members ADD COLUMN face_id TEXT;");
                        } catch (SQLException e) {
                                // Ignore "duplicate column name" error which happens if column already exists
                                if (!e.getMessage().contains("duplicate column")) {
                                        // System.out.println("Migration warning: " + e.getMessage());
                                }
                        }
                } catch (SQLException e) {
                        System.out.println(e.getMessage());
                }
        }
}
