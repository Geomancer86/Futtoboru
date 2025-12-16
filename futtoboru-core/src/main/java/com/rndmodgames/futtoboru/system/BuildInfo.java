package com.rndmodgames.futtoboru.system;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Build Information v1.0
 * 
 * Provides build metadata including version, build number, commit hash, and build timestamp.
 * This information is injected at build time and displayed in the game window title.
 * 
 * @author Geomancer86
 */
public class BuildInfo {
    
    private static final String BUILD_PROPERTIES_FILE = "/build.properties";
    
    private static String version = "0.4.0-SNAPSHOT";
    private static String buildNumber = "0";
    private static String commitHash = "unknown";
    private static String buildTimestamp = "unknown";
    
    private static boolean initialized = false;
    
    /**
     * Initialize build info from properties file
     * Called once at application startup
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            InputStream inputStream = BuildInfo.class.getResourceAsStream(BUILD_PROPERTIES_FILE);
            if (inputStream != null) {
                Properties props = new Properties();
                props.load(inputStream);
                inputStream.close();
                
                version = props.getProperty("version", version);
                buildNumber = props.getProperty("build.number", buildNumber);
                commitHash = props.getProperty("git.commit.id.abbrev", commitHash);
                buildTimestamp = props.getProperty("build.timestamp", buildTimestamp);
                
                // If commit hash is still unknown, try to get it from system property
                if ("unknown".equals(commitHash)) {
                    commitHash = System.getProperty("git.commit.id.abbrev", commitHash);
                }
            } else {
                // If properties file doesn't exist, try to get from system properties
                version = System.getProperty("project.version", version);
                buildNumber = System.getProperty("build.number", buildNumber);
                commitHash = System.getProperty("git.commit.id.abbrev", commitHash);
            }
        } catch (Exception e) {
            System.err.println("WARNING: Could not load build properties: " + e.getMessage());
            // Use defaults
        }
        
        initialized = true;
    }
    
    /**
     * Get full build info string for window title
     */
    public static String getBuildInfoString() {
        if (!initialized) {
            initialize();
        }
        
        return String.format("Futtoboru v%s | Build #%s | Commit: %s | %s", 
            version, buildNumber, commitHash, buildTimestamp);
    }
    
    /**
     * Get version string
     */
    public static String getVersion() {
        if (!initialized) {
            initialize();
        }
        return version;
    }
    
    /**
     * Get build number
     */
    public static String getBuildNumber() {
        if (!initialized) {
            initialize();
        }
        return buildNumber;
    }
    
    /**
     * Get commit hash
     */
    public static String getCommitHash() {
        if (!initialized) {
            initialize();
        }
        return commitHash;
    }
    
    /**
     * Get build timestamp
     */
    public static String getBuildTimestamp() {
        if (!initialized) {
            initialize();
        }
        return buildTimestamp;
    }
}

