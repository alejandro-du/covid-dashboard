package org.vaadin.covid.service.coronaapi.model;

import lombok.Data;

@Data
public class DataWrapper<T> {

    private T data;

}
