package org.vaadin.covid.service.coronaapi.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Timeline {

    private LocalDate date;
    private Long deaths;
    private Long confirmed;
    private Long recovered;
    private Long new_confirmed;
    private Long new_recovered;
    private Long new_deaths;

}
