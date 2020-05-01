package org.vaadin.covid.service.coronaapi;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.vaadin.covid.domain.Country;
import org.vaadin.covid.domain.Day;
import org.vaadin.covid.service.GeoIpService;
import org.vaadin.covid.service.coronaapi.model.LatestData;
import org.vaadin.covid.service.coronaapi.model.Timeline;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CovidService implements org.vaadin.covid.service.CovidService {

    public static final String COVID_SERVICE_CACHE = "covid-service-cache";

    private final WebService webService;

    public CovidService(WebService webService) {
        this.webService = webService;
    }

    @Override
    @Cacheable(cacheNames = COVID_SERVICE_CACHE)
    public List<Country> findAll() {
        return Stream.concat(
                Stream.of(getGlobal()),
                webService.countries().getData().stream()
                        .map(this::toDomain))
                .collect(Collectors.toList());
    }

    private Country getGlobal() {
        List<Timeline> timeLine = webService.timeline().getData();
        Timeline lastTimeLine = timeLine.get(0);

        LatestData latestData = new LatestData();
        latestData.setConfirmed(lastTimeLine.getConfirmed());
        latestData.setDeaths(lastTimeLine.getDeaths());
        latestData.setRecovered(lastTimeLine.getRecovered());

        org.vaadin.covid.service.coronaapi.model.Country world = new org.vaadin.covid.service.coronaapi.model.Country();
        world.setCode(GeoIpService.WORLD_ISO_CODE);
        world.setName("Global");
        world.setPopulation(7800000000L);
        world.setLatest_data(latestData);
        world.setTimeline(timeLine);

        return toDomain(world);
    }

    @Override
    @Cacheable(cacheNames = COVID_SERVICE_CACHE)
    public Country getById(String id) {
        if (GeoIpService.WORLD_ISO_CODE.equals(id)) {
            return getGlobal();
        } else {
            return toDomain(webService.countries(id).getData());
        }
    }

    private Country toDomain(org.vaadin.covid.service.coronaapi.model.Country c) {
        if (c != null) {
            List<Day> days = new ArrayList<>();
            if (c.getTimeline() != null) {
                days = c.getTimeline().stream()
                        .map(t -> new Day(
                                t.getDate(),
                                t.getConfirmed(),
                                t.getDeaths(),
                                t.getRecovered(),
                                t.getNew_confirmed(),
                                t.getNew_deaths(),
                                t.getNew_recovered()
                        ))
                        .collect(Collectors.toList());
            }

            return new Country(
                    c.getCode(),
                    c.getName(),
                    c.getPopulation(),
                    c.getLatest_data().getConfirmed(),
                    c.getLatest_data().getDeaths(),
                    c.getLatest_data().getRecovered(),
                    days
            );
        } else {
            return null;
        }
    }

    @Scheduled(cron = "${coronaapi.cache.evict.cron}")
    @CacheEvict(cacheNames = COVID_SERVICE_CACHE, allEntries = true)
    public void clearCache() {
    }

}
