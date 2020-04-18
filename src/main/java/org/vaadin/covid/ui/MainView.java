package org.vaadin.covid.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Theme(value = Lumo.class, variant = Lumo.DARK)
@CssImport("./css/styles.css")
@CssImport(value = "./css/charts.css", themeFor = "vaadin-chart", include = "vaadin-chart-default-theme")
@Route("")
@PWA(name = "Covid Dashboard", shortName = "Covid", description = "A Covid-19 dashboard app")
@PageTitle("Covid Dashboard")
public class MainView extends VerticalLayout {

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI.getCurrent().navigate(PlaceView.class);
    }

}
