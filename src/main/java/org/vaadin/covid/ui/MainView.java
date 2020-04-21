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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.vaadin.covid.domain.Area;
import org.vaadin.covid.domain.Stats;
import org.vaadin.covid.service.CovidService;

import java.util.List;
import java.util.Optional;

@Theme(value = Lumo.class, variant = Lumo.DARK)
@CssImport(value = "./css/styles.css", include = "vaadin-chart-default-theme")
@CssImport(value = "./css/charts.css", themeFor = "vaadin-chart", include = "vaadin-chart-default-theme")
@PWA(name = "Covid Dashboard", shortName = "Covid", description = "A Covid-19 dashboard app")
@Route("")
public class MainView extends VerticalLayout implements HasUrlParameter<String>, HasDynamicTitle {

    private final CovidService covidService;

    private Row overviewRow = new Row();
    private Row chartRow = new Row();
    private List<Area> places;
    private ComboBox<Area> areaSelector;

    public MainView(CovidService covidService) {
        this.covidService = covidService;
        places = covidService.findAllAreas();

        Image icon = new Image("icons/icon-small.png", "Icon");
        icon.addClassName("icon");
        HorizontalLayout title = new HorizontalLayout(
                new H1("Covid-19 Dashboard"),
                icon
        );
        title.addClassName("title");
        title.setVerticalComponentAlignment(Alignment.END, icon);

        areaSelector = new ComboBox<>();
        areaSelector.addClassName("place");
        areaSelector.setItems(places);
        areaSelector.setItemLabelGenerator(Area::getName);
        areaSelector.setPlaceholder("Place");

        Board board = new Board();
        board.addRow(areaSelector);
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

        areaSelector.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                UI.getCurrent().navigate(MainView.class, areaSelector.getValue().getIsoCode());
            }
        });
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String isoCode) {
        Optional<Area> placeById = covidService.getById(isoCode);
        setArea(placeById.orElse(covidService.getClosest(getIP())));
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
        return "Covid Dashboard - " + areaSelector.getValue().getName();
    }

    public void setArea(Area area) {
        if (area != null) {
            areaSelector.setValue(area);
            overviewRow.removeAll();

            if (area.getPopulation() != 0) {
                overviewRow.add(
                        new Stat("Population", area.getPopulation(), null, "number-population")
                );
            }
            overviewRow.add(
                    new Stat("Cases", area.getTotalCases(), area.getPopulation(), "number-cases"),
                    new Stat("Deaths", area.getTotalDeaths(), area.getTotalCases(), "number-deaths"),
                    new Stat("Recovered", area.getTotalRecovered(), area.getTotalCases(), "number-recovered")
            );
            chartRow.removeAll();
            chartRow.add(new Chart(
                    "Cumulative",
                    ChartType.SPLINE,
                    area.getTimeline(),
                    Stats::getCases,
                    Stats::getDeaths,
                    Stats::getRecovered
            ));

            if (area.getTimeline().size() >= 2) {
                int days;
                if (area.getTimeline().size() <= 7) {
                    days = area.getTimeline().size();
                } else {
                    days = 8;
                }
                List<Stats> timeline = area.getTimeline().subList(0, days);
                chartRow.add(new Chart(
                        "Daily",
                        ChartType.COLUMN,
                        timeline,
                        Stats::getNewCases,
                        Stats::getNewDeaths,
                        Stats::getNewRecovered
                ));
            }

        }
    }

}
