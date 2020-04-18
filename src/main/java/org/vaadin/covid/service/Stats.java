package org.vaadin.covid.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stats {

    private String date;

    private long cases;

    private long deaths;

    private long recovered;

    public Stats difference(Stats stats) {
        return new Stats(date, cases - stats.getCases(), deaths - stats.getDeaths(), recovered - stats.getRecovered());
    }

}
