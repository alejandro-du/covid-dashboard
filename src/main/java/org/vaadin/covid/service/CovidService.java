package org.vaadin.covid.service;

import org.vaadin.covid.domain.Country;

import java.util.List;

public interface CovidService {

    List<Country> findAll();

    Country getById(String id);

}
