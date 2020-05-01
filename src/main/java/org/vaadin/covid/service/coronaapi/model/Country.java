package org.vaadin.covid.service.coronaapi.model;

import lombok.Data;

import java.util.List;

@Data
public class Country implements Comparable<Country> {

    private Coordinates coordinates;
    private String name;
    private String code;
    private Long population;
    private LatestData latest_data;
    private List<Timeline> timeline;

    @Override
    public int compareTo(Country country) {
        return name.compareTo(country.getName());
    }

}
