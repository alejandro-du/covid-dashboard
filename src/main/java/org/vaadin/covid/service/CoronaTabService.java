package org.vaadin.covid.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "corona-tab", url = "https://api.coronatab.app")
public interface CoronaTabService {

    @Cacheable(cacheNames = "covid")
    @RequestMapping(value = "/places", headers = "User-Agent=Mozilla/5.0")
    DataWrapper<Place> findAllPlaces();

    @Cacheable(cacheNames = "covid")
    @RequestMapping(value="/places/closest", headers = "User-Agent=Mozilla/5.0")
    DataWrapper<Place> getClosest(@RequestHeader("X-Forwarded-For") String ip);

    @Cacheable(cacheNames = "covid")
    @RequestMapping(value="/places/{placeId}/data", headers = "User-Agent=Mozilla/5.0")
    DataWrapper<Stats> getStats(@PathVariable String placeId);

}
