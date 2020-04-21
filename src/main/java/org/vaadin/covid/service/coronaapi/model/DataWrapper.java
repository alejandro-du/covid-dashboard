
package org.vaadin.covid.service.coronaapi.model;

import lombok.Data;

import java.util.List;

@Data
public class DataWrapper<T> {

    private T data;

}
