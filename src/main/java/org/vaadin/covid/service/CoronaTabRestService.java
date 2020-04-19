package org.vaadin.covid.service;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "corona-tab", url = "https://api.coronatab.app")
public interface CoronaTabRestService {

    @Data
    class DataWrapper<T> {
        private List<T> data;
    }

    @RequestMapping(value = "/places", headers = "User-Agent=Mozilla/5.0")
    DataWrapper<Place> findAllPlaces();

    @RequestMapping(value = "/places/closest", headers = "User-Agent=Mozilla/5.0")
    DataWrapper<Place> getClosest(@RequestHeader("X-Forwarded-For") String ip);

    @RequestMapping(value = "/places/{placeId}/data", headers = "User-Agent=Mozilla/5.0")
    DataWrapper<Stats> getStats(@PathVariable String placeId);

}
