package org.vaadin.covid.service;

import org.vaadin.covid.domain.Country;

import java.util.List;
import java.util.Optional;

public interface CovidService {

    List<Country> findAll();

    Optional<Country> getById(String id);

    Country getClosest(String ip);

}
