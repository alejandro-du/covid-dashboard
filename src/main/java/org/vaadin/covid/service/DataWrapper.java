package org.vaadin.covid.service;

import lombok.Data;

import java.util.List;

@Data
public class DataWrapper<T> {

    List<T> data;

}
