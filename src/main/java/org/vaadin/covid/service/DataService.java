package org.vaadin.covid.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataService {

    public static final String CORONA_TAB_CACHE = "corona-tab";

    private final CoronaTabRestService restService;

    public DataService(CoronaTabRestService restService) {
        this.restService = restService;
    }

    @Cacheable(cacheNames = CORONA_TAB_CACHE)
    public List<Place> findAllPlaces() {
        return restService.findAllPlaces().getData();
    }

    public Place getClosest(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return restService.findAllPlaces().getData().get(0);
        }
        return restService.getClosest(ip).getData().get(0);
    }

    @Cacheable(cacheNames = CORONA_TAB_CACHE)
    public List<Stats> getStats(String placeId) {
        return restService.getStats(placeId).getData();
    }

    @Scheduled(cron = "0 0 8 * * *")
    @CacheEvict(cacheNames = CORONA_TAB_CACHE, allEntries = true)
    public void clearCache() {
    }

}
