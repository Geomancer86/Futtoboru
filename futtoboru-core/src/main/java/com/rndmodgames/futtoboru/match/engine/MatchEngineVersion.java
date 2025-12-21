package com.rndmodgames.futtoboru.match.engine;

import com.rndmodgames.futtoboru.system.BuildInfo;

/**
 * Match Engine Version Information
 * 
 * Critical component - keep track of versions and build numbers
 * for match engine compatibility and debugging.
 * 
 * Integrates with BuildInfo to get actual build data from build.properties
 * 
 * @author Geomancer86
 */
public class MatchEngineVersion {
    
    /**
     * Match Engine Version
     * Format: MAJOR.MINOR.PATCH
     * - MAJOR: Breaking changes
     * - MINOR: New features, backward compatible
     * - PATCH: Bug fixes, backward compatible
     */
    public static final String VERSION = "0.1.0";
    
    /**
     * Match Engine Architecture Version
     * Tracks the underlying engine architecture version
     * Based on DarkBlade engine analysis
     */
    public static final String ARCHITECTURE_VERSION = "DarkBlade-v1";
    
    /**
     * Get full version string with build info
     * Includes match engine version and build system info
     */
    public static String getFullVersionString() {
        // Ensure BuildInfo is initialized
        BuildInfo.initialize();
        
        return String.format("Match Engine v%s (Build %s, %s, %s)", 
            VERSION, 
            BuildInfo.getBuildNumber(),
            BuildInfo.getBuildTimestamp(),
            ARCHITECTURE_VERSION);
    }
    
    /**
     * Get version string for display
     */
    public static String getVersionString() {
        return "v" + VERSION;
    }
    
    /**
     * Get build info string (for bottom corner display)
     * Format: Match Engine v0.1.0 | Build #429 | Commit: 8c15252 | 2025-12-20 00:05:02
     */
    public static String getBuildInfoString() {
        // Ensure BuildInfo is initialized
        BuildInfo.initialize();
        
        return String.format("Match Engine v%s | Build #%s | Commit: %s | %s",
            VERSION,
            BuildInfo.getBuildNumber(),
            BuildInfo.getCommitHash(),
            BuildInfo.getBuildTimestamp());
    }
    
    /**
     * Get compact build info for small display
     * Format: ME v0.1.0 (#429)
     */
    public static String getCompactBuildInfoString() {
        BuildInfo.initialize();
        return String.format("ME v%s (#%s)", VERSION, BuildInfo.getBuildNumber());
    }
    
    /**
     * Get match engine version only (major.minor.patch)
     */
    public static String getVersion() {
        return VERSION;
    }
    
    /**
     * Get build number from build system
     */
    public static String getBuildNumber() {
        BuildInfo.initialize();
        return BuildInfo.getBuildNumber();
    }
    
    /**
     * Get commit hash from build system
     */
    public static String getCommitHash() {
        BuildInfo.initialize();
        return BuildInfo.getCommitHash();
    }
    
    /**
     * Get build timestamp from build system
     */
    public static String getBuildTimestamp() {
        BuildInfo.initialize();
        return BuildInfo.getBuildTimestamp();
    }
}
