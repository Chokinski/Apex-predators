package com.jat;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;



/**
 * The PlotHandler class is responsible for initializing and managing the OHLCChart within a JavaFX application.
 * It provides methods to determine the minimum and maximum date and price values from OHLC data, update the chart’s 
 * axis ranges, and seamlessly integrate the chart into a JavaFX pane.
 * 
 * <p>Features:
 * <ul>
 * <li>Calculates the minimum and maximum date ranges from OHLC data.</li>
 * <li>Determines the minimum and maximum price values with a small padding for better visualization.</li>
 * <li>Updates axis ranges dynamically to accommodate new data.</li>
 * <li>Displays the OHLCChart in a JavaFX pane while ensuring proper resizing behavior.</li>
 * <li>Supports smooth integration into a ScrollPane with adjustable fit settings.</li>
 * </ul>
 * </p>
 * 
 * <p>Usage:
 * <pre>
 * {@code
 * PlotHandler plotHandler = new PlotHandler();
 * ObservableList<OHLCData> ohlcDataList = FXCollections.observableArrayList();
 * // From compatible data source, add each to the datalist as an OHLCData object with timestamp, open, high, low, close, and volume values
 * for(Data data : dataList) {
 *      LocalDateTime timestamp = data.getTimestamp();
 *      double open = data.getOpen();
 *      double high = data.getHigh();
 *      double low = data.getLow();
 *      double close = data.getClose();
 *      double volume = data.getVolume();
 *      OHLCData ohlcData = new OHLCData(timestamp, open, high, low, close, volume);
 *      ohlcDataList.add(ohlcData);
 *      }
 * ScrollPane scrollPane = new ScrollPane();
 * AnchorPane chartPane = new AnchorPane();
 * plotHandler.showOHLCChart(scrollPane, chartPane, true, 50, ohlcDataList);
 * }
 * </pre>
 * </p>
 * 
 * <p>Methods:
 * <ul>
 * <li>{@link #getMinMaxDates(List)} - Returns the minimum and maximum date values from the dataset.</li>
 * <li>{@link #getMinMaxVals(List)} - Returns the minimum and maximum price values with padding.</li>
 * <li>{@link #updateAxesRanges()} - Updates the axis range of the OHLC chart.</li>
 * <li>{@link #showOHLCChart(ScrollPane, AnchorPane, boolean, int, ObservableList)} - Initializes and displays the OHLC chart.</li>
 * <li>{@link #displayChart(AnchorPane)} - Ensures the chart is added to the pane if not already present.</li>
 * <li>{@link #setResizable(boolean)} - Enables or disables resizable behavior for the ScrollPane.</li>
 * <li>{@link #setParent(ScrollPane)} - Sets the parent ScrollPane that contains the chart.</li>
 * <li>{@link #getOHLCChart()} - Returns the current OHLCChart instance.</li>
 * </ul>
 * </p>
 * 
 * <p>Dependencies:
 * <ul>
 * <li>{@link OHLCChart} - The chart that is being managed by this handler.</li>
 * <li>{@link DateTimeAxis} - The X-axis representing time.</li>
 * <li>{@link CurrencyAxis} - The Y-axis representing price.</li>
 * <li>{@link OHLCData} - The data model containing OHLC values.</li>
 * </ul>
 * </p>
 * 
 * @param parent The ScrollPane containing the chart.
 * @param pane The AnchorPane to display the chart.
 * @param resizable Whether the chart should resize dynamically.
 * @param pageSize The number of data points displayed at a time.
 * @param ohlcDataList The OHLC data set used to populate the chart.
 */

 
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
private Double[] getMinMaxVals(List<OHLCData> ohlcdatalist) {
    double minVal = Double.MAX_VALUE;
    double maxVal = Double.MIN_VALUE;

    for (OHLCData ohlcData : ohlcdatalist) {
        double lval = ohlcData.getLow();
        double hval = ohlcData.getHigh();
        if (lval < minVal) {
            minVal = lval;
        }
        if (hval > maxVal) {
            maxVal = hval;
        }
    }

    // Add padding (2% of the range) to avoid clipping
    double range = maxVal - minVal;
    double padding = range * 0.2; // 2% padding

    return new Double[] { minVal - padding, maxVal + padding };
}

    public void updateAxesRanges() {
    chart.updateAxisRange();
    }

    public ObservableList<OHLCData> readData() {
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

    public void showOHLCChart(ScrollPane parent, AnchorPane pane, boolean resizable,
        ObservableList<OHLCData> ohlcDataList) throws IOException {
        LocalDateTime[] dates = getMinMaxDates(ohlcDataList);
        Double[] vals = getMinMaxVals(ohlcDataList);
        DateTimeAxis xAxis = new DateTimeAxis(dates[0], dates[1],chart);
        CurrencyAxis yAxis = new CurrencyAxis(vals[0], vals[1],chart);

        this.chart = new OHLCChart(xAxis, yAxis, ohlcDataList,pane);

        chart.setSeries(ohlcDataList);
        System.out.println("Chart series set");

        displayChart(pane);

        chart.prefWidthProperty().bind(pane.widthProperty());
        chart.prefHeightProperty().bind(pane.heightProperty());
        chart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        System.out.println("Bounds set.");
        setParent(parent);
        System.out.println("Parent set.");
        setResizable(resizable);
        System.out.println("Resizable set.");
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