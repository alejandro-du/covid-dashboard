package org.vaadin.covid.service.coronaapi;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.vaadin.covid.service.coronaapi.model.Country;
import org.vaadin.covid.service.coronaapi.model.DataWrapper;

import java.util.List;

@FeignClient(name = "coronaapi", url = "${coronaapi.url}")
public interface WebService {

    @RequestMapping(value = "/countries")
    DataWrapper<List<Country>> countries();

    @RequestMapping(value = "/countries/{code}")
    DataWrapper<Country> countries(@PathVariable String code);

}
