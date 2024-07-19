package com.jat;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PlotHandler {
    private OHLCChart chart;

    @FXML
    private ScrollPane parent;

    public PlotHandler() {
    }

    private LocalDateTime[] getMinMaxDates(List<OHLCData> ohlcDataList) {
        LocalDateTime minDate = LocalDateTime.MAX;
        LocalDateTime maxDate = LocalDateTime.MIN;

        for (OHLCData ohlcData : ohlcDataList) {
            LocalDateTime date = ohlcData.getDateTime();
            if (date.isBefore(minDate)) {
                minDate = date;
            }
            if (date.isAfter(maxDate)) {
                maxDate = date;
            }
        }

        return new LocalDateTime[] { minDate, maxDate };
    }

    public void updateAxesRanges() {
    chart.updateAxisRange();
    }

    public ObservableList<OHLCData> mockData() {
        ObservableList<OHLCData> ohlcData = FXCollections.observableArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    
        try (InputStream is = getClass().getResourceAsStream("/data.txt");
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader reader = new BufferedReader(isr)) {
    
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace("OHLCData{", "").replace("}", "").trim();
                String[] parts = line.split(", ");
    
                String timestampStr = parts[0].substring(parts[0].indexOf('=') + 1).trim();
                LocalDateTime timestamp = LocalDateTime.parse(timestampStr, formatter);
    
                double open = Double.parseDouble(parts[1].substring(parts[1].indexOf('=') + 1));
                double high = Double.parseDouble(parts[2].substring(parts[2].indexOf('=') + 1));
                double low = Double.parseDouble(parts[3].substring(parts[3].indexOf('=') + 1));
                double close = Double.parseDouble(parts[4].substring(parts[4].indexOf('=') + 1));
                double volume = Double.parseDouble(parts[5].substring(parts[5].indexOf('=') + 1));
    
                OHLCData ohlc = new OHLCData(timestamp, open, high, low, close, volume);
                ohlcData.add(ohlc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        return ohlcData;
    }

    public void showOHLCChart(ScrollPane parent, AnchorPane pane, boolean resizable, int pageSize,
        ObservableList<OHLCData> ohlcDataList) throws IOException {
        LocalDateTime[] dates = getMinMaxDates(ohlcDataList);
        DateTimeAxis xAxis = new DateTimeAxis();
        CurrencyAxis yAxis = new CurrencyAxis();

        this.chart = new OHLCChart(xAxis, yAxis, ohlcDataList);
        chart.setSeries(ohlcDataList);
        displayChart(pane);

        chart.prefWidthProperty().bind(pane.widthProperty());
        chart.prefHeightProperty().bind(pane.heightProperty());
        chart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        setParent(parent);
        setResizable(resizable);
    }

    public void displayChart(AnchorPane pane) {
        if (!pane.getChildren().contains(chart)) {
            pane.getChildren().add(chart);
            System.out.println("Chart added to pane");
        } else {
            System.out.println("Chart already in pane");
        }
    }

    public void setResizable(boolean resizable) {
        this.parent.setFitToHeight(resizable);
        this.parent.setFitToWidth(resizable);
    }

    public void setParent(ScrollPane parent) {
        this.parent = parent;
    }

    public OHLCChart getOHLCChart() {
        return this.chart;
    }
}