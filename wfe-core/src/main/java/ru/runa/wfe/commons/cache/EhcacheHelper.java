/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.commons.cache;

import java.io.InputStream;
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
            CacheManager ehcacheManager = new CacheManager(configuration);
            log.info("EHCache manager loaded and will be used in WFE caches.");
            for (String cacheName : ehcacheManager.getCacheNames()) {
                log.debug("Found ehcache for WFE caching: " + cacheName);
            }
            return ehcacheManager;
        } catch (Throwable e) {
            log.error("Failed to create EHCache manager for WFE caching. Local caching will be used.", e);
        }
        return null;
    }
}
