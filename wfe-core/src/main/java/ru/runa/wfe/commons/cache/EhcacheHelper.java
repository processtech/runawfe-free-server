package ru.runa.wfe.commons.cache;

import java.io.InputStream;
import lombok.val;
import lombok.extern.apachecommons.CommonsLog;
import net.sf.ehcache.CacheManager;
import ru.runa.wfe.commons.ClassLoaderUtil;

/**
 * Helper to save caching information using EHCache.
 * 
 * @author Konstantinov Aleksey
 */
@CommonsLog
public final class EhcacheHelper {

    /**
     * {@linkplain CacheManager} to be used in WFE caches.
     */
    private static final CacheManager cacheManager = createManager();

    /**
     * Get {@linkplain CacheManager} to be used in WFE caches. May be null, if
     * no settings found.
     * 
     * @return {@linkplain CacheManager} to be used in WFE caches.
     */
    public static CacheManager getCacheManager() {
        return cacheManager;
    }

    /**
     * Creates {@linkplain CacheManager} according to settings file.
     * 
     * @return Created {@linkplain CacheManager} or null if creation failed.
     */
    private static CacheManager createManager() {
        try {
            InputStream configuration = ClassLoaderUtil.getAsStreamNotNull("hibernate.cache.xml", EhcacheHelper.class);
            val cm = CacheManager.create(configuration);
            log.info("EHCache manager loaded and will be used in WFE caches.");
            for (String cacheName : cm.getCacheNames()) {
                log.debug("Found ehcache for WFE caching: " + cacheName);
            }
            return cm;
        } catch (Throwable e) {
            log.error("Failed to create EHCache manager for WFE caching. Local caching will be used.", e);
        }
        return null;
    }
}
