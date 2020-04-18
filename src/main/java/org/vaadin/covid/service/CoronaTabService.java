package org.vaadin.covid.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name="corona-tab", url="https://api.coronatab.app")
public interface CoronaTabService {

    @RequestMapping("/places")
    DataWrapper<Place> findAllPlaces();

    @RequestMapping("/places/closest")
    DataWrapper<Place> getClosest();

    @RequestMapping("/places/{placeId}/data")
    DataWrapper<Stats> getStats(@PathVariable String placeId);

}
