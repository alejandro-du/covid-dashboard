package org.vaadin.covid.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

@Log4j2
@Service
public class GeoIpService {

    private DatabaseReader dbReader;

    public GeoIpService(@Value("${geoLite2.city.mmdb.database.location}") String geoLiteDatabaseLocation) {
        try {
            File file = new File(System.getProperty("user.home") + geoLiteDatabaseLocation);
            dbReader = new DatabaseReader.Builder(file).build();
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    public String getIsoCode(String ip) {
        String isoCode = "XX";

        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            CountryResponse country = dbReader.country(inetAddress);
            if (country != null) {
                isoCode = country.getCountry().getIsoCode();
            }

        } catch (IOException | GeoIp2Exception e) {
            e.printStackTrace();
        }

        return isoCode;
    }

}
