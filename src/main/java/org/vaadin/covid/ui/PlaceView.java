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
import org.vaadin.covid.service.CoronaTabService;
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
@PageTitle("Covid Dashboard")
public class PlaceView extends VerticalLayout implements HasUrlParameter<String> {

    private final CoronaTabService coronaTabService;

    private Row overviewRow = new Row();
    private Row chartRow = new Row();
    private List<Place> places;
    private ComboBox<Place> placeSelector;

    public PlaceView(CoronaTabService coronaTabService) {
        this.coronaTabService = coronaTabService;
        places = coronaTabService.findAllPlaces().getData();

        H1 title = new H1("Covid-19 Dashboard");

        placeSelector = new ComboBox<>();
        placeSelector.setClassName("place");
        placeSelector.setItems(places);
        placeSelector.setItemLabelGenerator(Place::getName);
        placeSelector.setPlaceholder("Place");

        Board board = new Board();
        board.addRow(placeSelector);
        board.addRow(overviewRow);
        board.addRow(chartRow);

        Image logoImage = new Image("images/vaadin.png", "Vaadin logo");
        logoImage.setClassName("logo");
        Anchor logo = new Anchor("https://vaadin.com", logoImage);

        HorizontalLayout footer = new HorizontalLayout(
                new Text("Powered by"),
                logo,
                new Anchor("https://github.com/alejandro-du/covid-dashboard", "Browse the source code.")
        );
        footer.setClassName("footer");
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
        Optional<Place> place = places.stream().filter(p -> p.getId().equals(placeId)).findFirst();
        if (place.isPresent()) {
            setPlace(place.get());
        } else {
            String ip = VaadinRequest.getCurrent().getHeader("X-Forwarded-For");
            setPlace(coronaTabService.getClosest(ip).getData().get(0));
        }
    }

    public void setPlace(Place place) {
        placeSelector.setValue(place);
        Stats stats = place.getLatestData();
        List<Stats> statsList = coronaTabService.getStats(place.getId()).getData();

        overviewRow.removeAll();
        overviewRow.add(
                getStat("Population", place.getPopulation(), null),
                getStat("Cases", stats.getCases(), place.getPopulation()),
                getStat("Deaths", stats.getDeaths(), place.getPopulation()),
                getStat("Recovered", stats.getRecovered(), place.getPopulation())
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

    private Component getStat(String description, long value, Long total) {
        Div descriptionDiv = new Div(new Text(description));
        descriptionDiv.setClassName("stat-description");

        String percentageText;
        if (total != null) {

            double percentage = (double) value / total * 100;
            String percentageStr = new DecimalFormat("#.##").format(percentage);
            percentageText = "(" + percentageStr + "%)";
        } else {
            percentageText = "";
        }

        Div percentageDiv = new Div(new Text(percentageText));
        percentageDiv.setClassName("stat-percentage");

        Div valueDiv = new Div(new Text(NumberFormat.getInstance().format(value)));
        valueDiv.setClassName("stat-value");

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
        layout.setClassName("chart");
        layout.setPadding(false);
        return new VerticalLayout(layout);
    }

}
