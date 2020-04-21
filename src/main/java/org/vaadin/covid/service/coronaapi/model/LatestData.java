package org.vaadin.covid.service.coronaapi.model;

import lombok.Data;

@Data
public class LatestData {

    private Long deaths;
    private Long confirmed;
    private Long recovered;

}
