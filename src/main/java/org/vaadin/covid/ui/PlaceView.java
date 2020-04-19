package org.vaadin.covid.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.VaadinRequest;
import org.vaadin.covid.service.DataService;
import org.vaadin.covid.service.Place;
import org.vaadin.covid.service.Stats;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route("place")
public class PlaceView extends VerticalLayout implements HasUrlParameter<String>, HasDynamicTitle {

    private final DataService dataService;

    private Row overviewRow = new Row();
    private Row chartRow = new Row();
    private List<Place> places;
    private ComboBox<Place> placeSelector;

    public PlaceView(DataService dataService) {
        this.dataService = dataService;
        places = dataService.findAllPlaces();

        Image icon = new Image("icons/icon-small.png", "Icon");
        icon.addClassName("icon");
        HorizontalLayout title = new HorizontalLayout(
                new H1("Covid-19 Dashboard"),
                icon
        );
        title.addClassName("title");
        title.setVerticalComponentAlignment(Alignment.END, icon);

        placeSelector = new ComboBox<>();
        placeSelector.addClassName("place");
        placeSelector.setItems(places);
        placeSelector.setItemLabelGenerator(Place::getName);
        placeSelector.setPlaceholder("Place");

        Board board = new Board();
        board.addRow(placeSelector);
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

        placeSelector.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                UI.getCurrent().navigate(PlaceView.class, placeSelector.getValue().getId());
            }
        });
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String placeId) {
        Optional<Place> placeById = places.stream().filter(p -> p.getId().equals(placeId)).findFirst();
        setPlace(placeById.orElse(
                dataService.getClosest(VaadinRequest.getCurrent().getHeader("X-Forwarded-For"))
        ));
    }

    @Override
    public String getPageTitle() {
        return "Covid Dashboard - " + placeSelector.getValue().getName();
    }

    public void setPlace(Place place) {
        placeSelector.setValue(place);
        Stats stats = place.getLatestData();
        List<Stats> statsList = dataService.getStats(place.getId());

        overviewRow.removeAll();
        if (place.getPopulation() != 0) {
            overviewRow.add(
                    getStat("Population", place.getPopulation(), null, "number-population")
            );
        }
        overviewRow.add(
                getStat("Cases", stats.getCases(), place.getPopulation(), "number-cases"),
                getStat("Deaths", stats.getDeaths(), stats.getCases(), "number-deaths"),
                getStat("Recovered", stats.getRecovered(), stats.getCases(), "number-recovered")
        );
        chartRow.removeAll();
        List<Stats> lastDaysDifferenceStats = new ArrayList<>();

        if (statsList.size() >= 2) {
            int days;
            if (statsList.size() <= 7) {
                days = statsList.size();
            } else {
                days = 8;
            }


            for (int i = days - 2; i >= 0; i--) {
                Stats stats1 = statsList.get(statsList.size() - i - 2);
                Stats stats2 = statsList.get(statsList.size() - i - 1);
                lastDaysDifferenceStats.add(stats2.difference(stats1));
            }
        }


        chartRow.add(
                getChart("Cumulative", ChartType.SPLINE, statsList),
                getChart("Daily", ChartType.COLUMN, lastDaysDifferenceStats)
        );
    }

    private Component getStat(String description, long value, Long total, String cssClass) {
        Div descriptionDiv = new Div(new Text(description));
        descriptionDiv.addClassName("stat-description");

        String percentageText = "";
        if (total != null && total != 0) {
            double percentage = (double) value / total * 100;
            String percentageStr = new DecimalFormat("#.##").format(percentage);
            percentageText = "(" + percentageStr + "%)";
        }

        Div valueDiv = new Div(new Text(NumberFormat.getInstance().format(value)));
        valueDiv.addClassName(cssClass);
        valueDiv.addClassName("stat-value");

        Div percentageDiv = new Div(new Text(percentageText));
        percentageDiv.addClassName(cssClass);
        percentageDiv.addClassName("stat-percentage");

        HorizontalLayout numbersLayout = new HorizontalLayout(
                valueDiv,
                percentageDiv
        );
        numbersLayout.setSpacing(false);

        VerticalLayout verticalLayout = new VerticalLayout(
                descriptionDiv,
                numbersLayout
        );
        verticalLayout.setSpacing(false);
        verticalLayout.setSizeUndefined();

        return verticalLayout;
    }

    private Component getChart(String title, ChartType chartType, List<Stats> statsList) {
        Chart chart = new Chart(chartType);
        chart.getElement().getStyle().set("--vaadin-charts-color-1", "#bb4444");

        Configuration configuration = chart.getConfiguration();
        configuration.getTooltip().setEnabled(true);
        configuration.getxAxis().setType(AxisType.DATETIME);
        configuration.setTitle(title);

        DataSeries cases = new DataSeries();
        cases.setName("Cases");
        DataSeries deaths = new DataSeries();
        deaths.setName("Deaths");
        DataSeries recovered = new DataSeries();
        recovered.setName("Recovered");

        for (Stats stats : statsList) {
            Instant instant = LocalDate.parse(stats.getDate()).atStartOfDay().toInstant(ZoneOffset.UTC);
            cases.add(new DataSeriesItem(instant, stats.getCases()));
            deaths.add(new DataSeriesItem(instant, stats.getDeaths()));
            recovered.add(new DataSeriesItem(instant, stats.getRecovered()));
        }

        configuration.addSeries(cases);
        configuration.addSeries(deaths);
        configuration.addSeries(recovered);

        VerticalLayout layout = new VerticalLayout(chart);
        layout.addClassName("chart");
        layout.setPadding(false);
        return new VerticalLayout(layout);
    }
}
