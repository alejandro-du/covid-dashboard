# covid-dashboard

A Covid-19 dashboard implemented with Spring Boot and Vaadin with PWA features.

## Building the app

```
git clone https://github.com/alejandro-du/covid-dashboard.git
```

Due to licensing policies, you have to download your own copy of the *GeoLite2 Cities* database at
https://dev.maxmind.com/geoip/geoip2/geolite2. Place the `GeoLite2-City.mmdb` file  in the `temp/`
directory of your home directory or configure the location in the project's
`application.properties` file (you have to use a path relative to your home directory).

```
mvn clean jetty:run
```
