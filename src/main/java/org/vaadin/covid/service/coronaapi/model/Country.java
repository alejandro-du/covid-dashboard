package org.vaadin.covid.service.coronaapi.model;

import lombok.Data;

import java.util.List;

@Data
public class Country {

    private Coordinates coordinates;
    private String name;
    private String code;
    private Long population;
    private LatestData latest_data;
    private List<Timeline> timeline;

}
