package com.jat;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OHLCChart extends XYChart<LocalDateTime, Double> {

    private ObservableList<OHLCData> ohlcDataList;
    protected CurrencyAxis yAxis;
    protected DateTimeAxis xAxis;

    public OHLCChart(DateTimeAxis xAxis, CurrencyAxis yAxis, ObservableList<OHLCData> ohlcDataList) {
        super(xAxis, yAxis);
        this.ohlcDataList = ohlcDataList;
        this.yAxis = yAxis;
        this.xAxis = xAxis;

        setTitle("OHLC Chart");
        xAxis.setLabel("Time");
        yAxis.setLabel("Price");

        setData(FXCollections.observableArrayList());

        // Initial series setup
        Series<LocalDateTime, Double> series = new Series<>();
        addDataToSeries(series);
        getData().add(series);

        // Ensure initial rendering
        Platform.runLater(this::layoutPlotChildren);
    }

        /*TODO: -For dev use, copy template [], allow copilot to check code if its done, if not, copi states [NEED] if finished and states [NEED] copilot will update to [DONE]- 
        
        When given data, take a set amount of data points and display them on the chart, percentage perhaps? [DONE]
        *  This will be useful for the initial display of the chart, as well as for the slider functionality that will be implemented later on. [DONE]
        *  The data will be displayed in the form of candlesticks, with each candlestick representing a set amount of data points. [DONE]
        *  The candlesticks will be colored based on the close value of the data point. [DONE]
        *  The candlesticks will be displayed in the chart area, with the x-axis representing the time and the y-axis representing the price. [DONE]
        *  The chart will be updated with new data points as they are received. [DONE]
        *  The chart... able to display a large amount of data points without performance issues. [NEED] 
        *  The chart... able to display data points in real-time. [NEED]
        *  The chart... visually appealing and easy to read. 
        *  The chart... easy to interact with, such as zooming in and out, panning, and selecting data points. [NEED]
        *  The chart... customizable, such as changing the color scheme, the time period displayed, and the type of chart used. [NEED]
        *  The chart... responsive to user input, such as resizing the chart area, changing the time period displayed, and selecting data points. 

        */
    public void setSeries(ObservableList<OHLCData> ohlcDataList) {
        this.ohlcDataList = ohlcDataList;
        Series<LocalDateTime, Double> series = new Series<>();
        addDataToSeries(series);

        Platform.runLater(() -> {
            ObservableList<Series<LocalDateTime, Double>> chartData = getData();
            if (chartData != null) {
                chartData.setAll(series);
                layoutPlotChildren();
            } else {
                System.err.println("Chart data is null. Cannot update series.");
            }
        });

        // Update axis range after setting new data
        updateAxisRange();
    }

    // Method to get the current chart data
    public List<XYChart.Series<LocalDateTime, Double>> getChartData() {
        return getData();
    }

    @Override
    protected void updateAxisRange() {
        xAxis.invalidateRangeInternal(this);
        yAxis.invalidateRangeInternal(this);
    }

    private void addDataToSeries(Series<LocalDateTime, Double> series) {
        if (ohlcDataList == null) {
            throw new IllegalStateException("ohlcDataList cannot be null");
        }

        List<XYChart.Data<LocalDateTime, Double>> dataToAdd = ohlcDataList.stream()
                .map(this::toChartData)
                .collect(Collectors.toList());

        series.getData().addAll(dataToAdd);
    }

    private XYChart.Data<LocalDateTime, Double> toChartData(OHLCData ohlcData) {
        return new XYChart.Data<>(ohlcData.getDateTime(), ohlcData.getClose());
    }

@Override
protected void layoutPlotChildren() {
    if (getData().isEmpty()) {
        return;
    }

    Series<LocalDateTime, Double> series = getData().get(0);
    getPlotChildren().clear();
        Map<LocalDateTime, OHLCData> ohlcDataMap = ohlcDataList.stream()
                .collect(Collectors.toMap(OHLCData::getDateTime, ohlcData -> ohlcData));
    // Use ohlcDataMap for fast lookup
    for (XYChart.Data<LocalDateTime, Double> item : series.getData()) {
        LocalDateTime dateTime = item.getXValue();
        Double closeValue = item.getYValue();

        if (dateTime == null || closeValue == null) {
            continue;
        }

        OHLCData ohlcData = ohlcDataMap.get(dateTime); // Retrieve OHLCData from map

        if (ohlcData == null) {
            continue;
        }

        Node node = createCandleStick(ohlcData);
        if (node != null) {
            getPlotChildren().add(node);
        }
    }
}

private Node createCandleStick(OHLCData ohlcData) {
    double openValue = ohlcData.getOpen();
    double highValue = ohlcData.getHigh();
    double lowValue = ohlcData.getLow();
    double closeValue = ohlcData.getClose();

    double candleWidth = calculateCandleWidth();
    double bodyHeight = Math.abs(yAxis.getDisplayPosition(openValue) - yAxis.getDisplayPosition(closeValue));
    double bodyY = Math.min(yAxis.getDisplayPosition(openValue), yAxis.getDisplayPosition(closeValue));

    double upperWickHeight = yAxis.getDisplayPosition(highValue) - bodyY;
    double lowerWickHeight = yAxis.getDisplayPosition(lowValue) - (bodyY + bodyHeight);

    // Create the candle body
    Rectangle body = new Rectangle();
    body.setWidth(candleWidth);
    body.setHeight(bodyHeight);
    body.setFill(closeValue >= openValue ? Color.GREEN : Color.RED);

    // Create the upper wick
    Line upperWick = new Line();
    upperWick.setStartX(candleWidth / 2);
    upperWick.setEndX(candleWidth / 2);
    upperWick.setStartY(0); // Start from the top of the body
    upperWick.setEndY(upperWickHeight);
    upperWick.setStroke(closeValue >= openValue ? Color.GREEN : Color.RED);

    // Create the lower wick
    Line lowerWick = new Line();
    lowerWick.setStartX(candleWidth / 2);
    lowerWick.setEndX(candleWidth / 2);
    lowerWick.setStartY(bodyHeight); // Start from the bottom of the body
    lowerWick.setEndY(bodyHeight + lowerWickHeight);
    lowerWick.setStroke(closeValue >= openValue ? Color.GREEN : Color.RED);

    // Create the candlestick group and add body and wicks
    Group candlestick = new Group();
    candlestick.getChildren().addAll(body, upperWick, lowerWick);
    candlestick.getStyleClass().add("candlestick");

    // Add tooltip
    Tooltip tooltip = new Tooltip(
            "DateTime: " + ohlcData.getDateTime() + "\n" +
            "Open: " + ohlcData.getOpen() + "\n" +
            "High: " + ohlcData.getHigh() + "\n" +
            "Low: " + ohlcData.getLow() + "\n" +
            "Close: " + ohlcData.getClose()
    );
    Tooltip.install(candlestick, tooltip);

    // Set candlestick position
    candlestick.setLayoutX(getXAxis().getDisplayPosition(ohlcData.getDateTime()) - candleWidth / 2);
    candlestick.setLayoutY(bodyY);

    return candlestick;
}



private double calculateCandleWidth() {
    if (ohlcDataList.size() < 2) {
        return 10; // Default width if there are less than 2 data points
    }

    double axisLength = xAxis.getWidth(); // get the width of the xAxis
    double numDataPoints = ohlcDataList.size();
    double candleWidthFraction = 0.2; // fraction of the space allocated to each candle

    double calculatedWidth = (axisLength / numDataPoints) * candleWidthFraction;
    double minCandleWidth = 1.36; // Minimum candle width
    //System.out.println("Calculated candle width: " + calculatedWidth);
    return Math.max(minCandleWidth, calculatedWidth);
}

    @Override
    protected void dataItemAdded(Series<LocalDateTime, Double> series, int itemIndex, Data<LocalDateTime, Double> item) {
        // Update the plot to reflect the new data item
        layoutPlotChildren();
    }

    @Override
    protected void dataItemChanged(Data<LocalDateTime, Double> item) {
        // Update the plot to reflect the changed data item
        layoutPlotChildren();
    }

    @Override
    protected void dataItemRemoved(Data<LocalDateTime, Double> item, Series<LocalDateTime, Double> series) {
        // Update the plot to reflect the removed data item
        layoutPlotChildren();
    }

    @Override
    protected void seriesAdded(Series<LocalDateTime, Double> series, int seriesIndex) {
        // Update the plot to reflect the added series
        layoutPlotChildren();
    }

    @Override
    protected void seriesRemoved(Series<LocalDateTime, Double> series) {
        // Update the plot to reflect the removed series
        layoutPlotChildren();
    }
}