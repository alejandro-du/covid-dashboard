package org.vaadin.covid.service;

import org.vaadin.covid.domain.Area;

import java.util.List;
import java.util.Optional;

public interface CovidService {

    List<Area> findAllAreas();

    Optional<Area> getById(String id);

    Area getClosest(String ip);

}
