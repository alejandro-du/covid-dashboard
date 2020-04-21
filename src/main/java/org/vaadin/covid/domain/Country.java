package org.vaadin.covid.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Country {

    @EqualsAndHashCode.Include
    private String isoCode;

    private String name;

    private Long population;

    private Long totalCases;

    private Long totalDeaths;

    private Long totalRecovered;

    private List<Day> days;

}
