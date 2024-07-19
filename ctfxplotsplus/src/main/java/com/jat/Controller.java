package com.jat;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Controller {

    @FXML
    private Font x1;

    @FXML
    private Color x2;

    @FXML
    private Font x3;

    @FXML
    private Color x4;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private AnchorPane anchorForChart;

    @FXML
    private Slider sliderSpacing;

    @FXML
    private Slider sliderWidth;

    private OHLCChart chart; // Ensure this is properly initialized

    @FXML
    void initialize() {
        try {
            PlotHandler plotHandler = new PlotHandler();
            System.out.println("\nmocking Data...\n\n");
            ObservableList<OHLCData> data = plotHandler.mockData();
            System.out.println("Showing chart....\n\n");
            plotHandler.showOHLCChart(scrollPane, anchorForChart, true, 20, data);
            this.chart = plotHandler.getOHLCChart();

            if (chart != null) {
                System.out.println("Chart initialized with data: " + data.size() + " points");

                // Ensure the chart is fully laid out and updated
                Platform.runLater(() -> {
                    chart.layoutPlotChildren();
                    chart.requestLayout();
                });
            } else {
                System.err.println("Chart instance is null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}