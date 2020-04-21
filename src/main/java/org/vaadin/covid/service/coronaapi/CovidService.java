package org.vaadin.covid.service.coronaapi;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.vaadin.covid.domain.Area;
import org.vaadin.covid.domain.Stats;
import org.vaadin.covid.service.coronaapi.model.Country;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CovidService implements org.vaadin.covid.service.CovidService {

    public static final String COVID_SERVICE_CACHE = "covid-service-cache";

    private final WebService webService;

    public CovidService(WebService webService) {
        this.webService = webService;
    }

    @Override
    public List<Area> findAllAreas() {
        return webService.countries().getData().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Area> getById(String id) {
        return Optional.of(toDomain(webService.countries(id).getData()));
    }

    @Override
    public Area getClosest(String ip) {
        return findAllAreas().get(0);
    }

    private Area toDomain(Country c) {
        if (c != null) {
            List<Stats> stats = new ArrayList<>();
            if (c.getTimeline() != null) {
                stats = c.getTimeline().stream()
                        .map(t -> new Stats(
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

            return new Area(
                    c.getCode(),
                    c.getName(),
                    c.getPopulation(),
                    c.getLatest_data().getConfirmed(),
                    c.getLatest_data().getDeaths(),
                    c.getLatest_data().getRecovered(),
                    stats
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
