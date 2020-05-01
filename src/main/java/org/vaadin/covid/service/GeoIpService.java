package org.vaadin.covid.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

@Slf4j
@Service
public class GeoIpService {

    public static final String WORLD_ISO_CODE = "global";

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
        String isoCode = WORLD_ISO_CODE;

        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            CountryResponse country = dbReader.country(inetAddress);
            if (country != null) {
                isoCode = country.getCountry().getIsoCode();
            }

        } catch (GeoIp2Exception ignored) {

        } catch (IOException e) {
            log.error("Error getting ISO code", e);
        }

        return isoCode;
    }

}
