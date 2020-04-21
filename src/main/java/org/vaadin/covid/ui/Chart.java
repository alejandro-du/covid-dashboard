package org.vaadin.covid.ui;

import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.vaadin.covid.domain.Stats;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Function;

public class Chart extends VerticalLayout {

    public Chart(String title, ChartType chartType, List<Stats> statsList, Function<Stats, Long> casesFunction, Function<Stats, Long> deathsFunction, Function<Stats, Long> recoveredFunction) {
        com.vaadin.flow.component.charts.Chart chart = new com.vaadin.flow.component.charts.Chart(chartType);
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
            Instant instant = stats.getDate().atStartOfDay().toInstant(ZoneOffset.UTC);
            cases.add(new DataSeriesItem(instant, casesFunction.apply(stats)));
            deaths.add(new DataSeriesItem(instant, deathsFunction.apply(stats)));
            recovered.add(new DataSeriesItem(instant, recoveredFunction.apply(stats)));
        }

        configuration.addSeries(cases);
        configuration.addSeries(deaths);
        configuration.addSeries(recovered);

        VerticalLayout layout = new VerticalLayout(chart);
        layout.addClassName("chart");
        layout.setPadding(false);
        add(new VerticalLayout(layout));
    }

}
