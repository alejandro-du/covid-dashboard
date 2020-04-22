package org.vaadin.covid.ui;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.vaadin.covid.domain.Day;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Function;

public class DashboardChart extends VerticalLayout {

    public DashboardChart(String title, ChartType chartType, List<Day> statsList, Function<Day, Long> casesFunction, Function<Day, Long> deathsFunction, Function<Day, Long> recoveredFunction) {
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

        for (Day day : statsList) {
            Instant instant = day.getDate().atStartOfDay().toInstant(ZoneOffset.UTC);
            cases.add(new DataSeriesItem(instant, casesFunction.apply(day)));
            deaths.add(new DataSeriesItem(instant, deathsFunction.apply(day)));
            recovered.add(new DataSeriesItem(instant, recoveredFunction.apply(day)));
        }

        configuration.addSeries(cases);
        configuration.addSeries(deaths);
        configuration.addSeries(recovered);

        VerticalLayout layout = new VerticalLayout(chart);
        layout.addClassName("chart");
        layout.setPadding(false);
        add(layout);
    }

}
