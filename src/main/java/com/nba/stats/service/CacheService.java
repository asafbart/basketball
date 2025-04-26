package com.nba.stats.service;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache service that uses a hybrid approach:
 * - Annotations for simple, single-cache operations (cleaner code)
 * - Direct CacheManager access for critical or complex operations (more reliable)
 */
@Service
public class CacheService {
    private final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private final CacheManager cacheManager;
    
    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    
    /**
     * Invalidates player cache using annotations for simple operations
     */
    @CacheEvict(cacheNames = "playerSeasonStats", key = "#playerId")
    public void invalidatePlayerCache(Long playerId) {
        logger.debug("Invalidating player cache for player ID: {}", playerId);
        // The annotation handles the cache eviction
    }
    
    /**
     * Invalidates team cache using annotations for simple operations
     */
    @CacheEvict(cacheNames = "teamSeasonStats", key = "#teamId")
    public void invalidateTeamCache(Long teamId) {
        logger.debug("Invalidating team cache for team ID: {}", teamId);
        // The annotation handles the cache eviction
    }
    
    /**
     * Invalidates all teams cache using annotations for simple operations
     */
    @CacheEvict(cacheNames = "allTeamSeasonStats", allEntries = true)
    public void invalidateAllTeamsCache() {
        logger.debug("Invalidating all teams cache");
        // The annotation handles the cache eviction
    }
    
    /**
     * CRITICAL OPERATION: Invalidates all related caches for a player and team
     * This uses direct CacheManager access for more reliable eviction when
     * multiple caches need to be updated together
     */
    public void invalidateStatsCache(Long playerId, Long teamId) {
        logger.info("Invalidating all stats caches for player ID: {} and team ID: {}", playerId, teamId);
        
        // Use direct CacheManager access for critical operations to ensure all caches are properly invalidated
        if (cacheManager.getCache("playerSeasonStats") != null) {
            cacheManager.getCache("playerSeasonStats").evict(playerId);
        }
        
        if (cacheManager.getCache("teamSeasonStats") != null) {
            cacheManager.getCache("teamSeasonStats").evict(teamId);
        }
        
        if (cacheManager.getCache("allTeamSeasonStats") != null) {
            cacheManager.getCache("allTeamSeasonStats").clear();
        }
        
        logger.debug("Successfully invalidated all related caches");
    }
    
    /**
     * CRITICAL OPERATION: Invalidates ALL caches in the system
     * Direct CacheManager approach for this system-wide operation
     */
    public void invalidateAllCaches() {
        logger.info("Invalidating ALL caches in the system");
        
        // Get all cache names and clear each one
        for (String cacheName : cacheManager.getCacheNames()) {
            logger.debug("Clearing cache: {}", cacheName);
            cacheManager.getCache(cacheName).clear();
        }
    }
}