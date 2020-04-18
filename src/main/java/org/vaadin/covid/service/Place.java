package org.vaadin.covid.service;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Place {

    @EqualsAndHashCode.Include
    private String id;

    private String name;

    private Stats latestData;

    private long population;

}
