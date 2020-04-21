package org.vaadin.covid.service.wipmania;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "wipmania", url = "${wipmania.url}")
public interface IpService extends org.vaadin.covid.service.IpService {

    @Override
    @RequestMapping(value = "/{ip}")
    String getIsoCode(@PathVariable String ip);

}
