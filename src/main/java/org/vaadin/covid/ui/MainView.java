package org.vaadin.covid.ui;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.covid.domain.Country;
import org.vaadin.covid.domain.Day;
import org.vaadin.covid.service.CovidService;
import org.vaadin.covid.service.GeoIpService;

import java.util.List;

@Slf4j
@Theme(value = Lumo.class, variant = Lumo.DARK)
@CssImport(value = "./css/styles.css", include = "vaadin-chart-default-theme")
@CssImport(value = "./css/charts.css", themeFor = "vaadin-chart", include = "vaadin-chart-default-theme")
@PWA(name = "Covid Dashboard", shortName = "Covid", description = "A Covid-19 dashboard app", display = "fullscreen")
@Route("")
public class MainView extends VerticalLayout implements HasUrlParameter<String>, HasDynamicTitle {

    private final CovidService covidService;

    private Row overviewRow = new Row();
    private Row chartRow = new Row();
    private ComboBox<Country> countrySelector;

    private final GeoIpService geoIpService;

    public MainView(GeoIpService geoIpService, CovidService covidService) {
        this.geoIpService = geoIpService;
        this.covidService = covidService;

        Image icon = new Image("icons/icon.png", "Icon");
        icon.addClassName("icon");
        HorizontalLayout title = new HorizontalLayout(
                new H1("Covid-19 Dashboard"),
                icon
        );
        title.addClassName("title");
        title.setVerticalComponentAlignment(Alignment.END, icon);

        countrySelector = new ComboBox<>();
        countrySelector.setItems(covidService.findAll());
        countrySelector.setItemLabelGenerator(Country::getName);
        countrySelector.setPlaceholder("Country");
        countrySelector.setClearButtonVisible(true);

        Board board = new Board();
        board.addRow(countrySelector);
        board.addRow(overviewRow);
        board.addRow(chartRow);

        Image vaadinImage = new Image("images/vaadin.png", "Vaadin logo");
        vaadinImage.addClassName("vaadin");
        Anchor vaadin = new Anchor("https://vaadin.com", vaadinImage);

        HorizontalLayout footer = new HorizontalLayout(
                new Text("Powered by"),
                vaadin,
                new Anchor("https://github.com/alejandro-du/covid-dashboard", "Browse the source code.")
        );
        footer.addClassName("footer");
        footer.setMargin(true);
        footer.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        add(
                new CookieConsent(),
                title,
                board,
                footer
        );

        countrySelector.addValueChangeListener(event -> {
            if (event.isFromClient() && countrySelector.getValue() != null) {
                UI.getCurrent().navigate(MainView.class, countrySelector.getValue().getIsoCode());
            }
        });
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String isoCode) {
        String ip = getIP();

        if (isoCode == null || isoCode.isEmpty()) {
            isoCode = geoIpService.getIsoCode(ip);
        }

        try {
            log.info(String.format("IP - ISO code: %s - %s", ip, isoCode));
            setCountry(covidService.getById(isoCode));

        } catch (FeignException e) {
            log.info("Cannot find ISO code: " + isoCode);
            setCountry(covidService.getById(GeoIpService.WORLD_ISO_CODE));
            Notification.show("Country not found. Showing global data.", 5000, Notification.Position.MIDDLE);

        } catch (Exception e) {
            log.error("Error fetching data", e);
            Notification.show("Error fetching data.", 5000, Notification.Position.MIDDLE);
        }
    }

    private String getIP() {
        String ip;

        if ((ip = VaadinRequest.getCurrent().getHeader("X-Forwarded-For")) == null) {
            if ((ip = VaadinRequest.getCurrent().getHeader("Via")) == null) {
                ip = VaadinRequest.getCurrent().getRemoteHost();
            }
        }

        return ip;
    }

    @Override
    public String getPageTitle() {
        return "Covid Dashboard - " + countrySelector.getValue().getName();
    }

    public void setCountry(Country country) {
        if (country != null) {
            countrySelector.setValue(country);
            overviewRow.removeAll();

            if (country.getPopulation() != null && country.getPopulation() != 0) {
                overviewRow.add(
                        new DashboardStats("Population", country.getPopulation(), null, "number-population")
                );
            }
            overviewRow.add(
                    new DashboardStats("Cases", country.getTotalCases(), country.getPopulation(), "number-cases"),
                    new DashboardStats("Deaths", country.getTotalDeaths(), country.getTotalCases(), "number-deaths"),
                    new DashboardStats("Recovered", country.getTotalRecovered(), country.getTotalCases(), "number-recovered")
            );
            chartRow.removeAll();
            chartRow.add(new DashboardChart(
                    "Cumulative",
                    ChartType.SPLINE,
                    country.getDays(),
                    Day::getCases,
                    Day::getDeaths,
                    Day::getRecovered
            ));

            if (country.getDays().size() >= 2) {
                int days;
                if (country.getDays().size() <= 7) {
                    days = country.getDays().size();
                } else {
                    days = 8;
                }
                List<Day> timeline = country.getDays().subList(0, days);
                chartRow.add(new DashboardChart(
                        "Daily",
                        ChartType.COLUMN,
                        timeline,
                        Day::getNewCases,
                        Day::getNewDeaths,
                        Day::getNewRecovered
                ));
            }

        }
    }

}
