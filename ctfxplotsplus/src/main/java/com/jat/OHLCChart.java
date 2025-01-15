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
import java.util.concurrent.CompletableFuture;
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

        // Initial rendering setup
        Series<LocalDateTime, Double> series = new Series<>();
        getData().add(series);

        // Incremental rendering
        //renderCandlesticksIncrementally();
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
        
            // Split the data into smaller chunks
            int chunkSize = 500; // Adjust as needed
            int totalChunks = (int) Math.ceil((double) ohlcDataList.size() / chunkSize);
        
            CompletableFuture.runAsync(() -> {
                for (int i = 0; i < totalChunks; i++) {
                    int start = i * chunkSize;
                    int end = Math.min(start + chunkSize, ohlcDataList.size());
                    ObservableList<OHLCData> chunk = FXCollections.observableArrayList(ohlcDataList.subList(start, end));
        
                    Platform.runLater(() -> {
                        Series<LocalDateTime, Double> series = new Series<>();
                        chunk.forEach(data -> series.getData().add(toChartData(data)));
                        getData().add(series);
                        
                    });
                     // Update the axis range dynamically
                    // Add a small delay if necessary for UI responsiveness
                    try {
                        Thread.sleep(50); // Adjust as needed
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            
        }
    private void renderCandlesticksIncrementally() {
        int chunkSize = 500; // Number of candlesticks per chunk
        int totalChunks = (int) Math.ceil((double) ohlcDataList.size() / chunkSize);

        CompletableFuture.runAsync(() -> {
            for (int i = 0; i < totalChunks; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, ohlcDataList.size());
                List<OHLCData> chunk = ohlcDataList.subList(start, end);

                Platform.runLater(() -> {
                    chunk.forEach(data -> {
                        Node candlestick = createCandleStick(data);
                        if (candlestick != null) {
                            getPlotChildren().add(candlestick);
                        }
                    });
                    
                });

                try {
                    Thread.sleep(50); // Adjust delay for smoother rendering
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
    }
    private XYChart.Data<LocalDateTime, Double> toChartData(OHLCData ohlcData) {
        return new XYChart.Data<>(ohlcData.getDateTime(), ohlcData.getClose());
    }
    @Override
    protected void layoutPlotChildren() {
        // Efficient rendering by skipping unnecessary operations
        if (ohlcDataList == null || ohlcDataList.isEmpty()) {
            return;
        }
        int chunkSize = 500; // Number of candlesticks per chunk
        int totalChunks = (int) Math.ceil((double) ohlcDataList.size() / chunkSize);
        getPlotChildren().clear();
        CompletableFuture.runAsync(() -> {
            for (int i = 0; i < totalChunks; i++) {
                int start = i * chunkSize;
                int end = Math.min(start + chunkSize, ohlcDataList.size());
                List<OHLCData> chunk = ohlcDataList.subList(start, end);

                Platform.runLater(() -> {
                    chunk.forEach(data -> {
                        Node candlestick = createCandleStick(data);
                        if (candlestick != null) {
                            getPlotChildren().add(candlestick);
                        }
                    });
                    
                });

                try {
                    Thread.sleep(50); // Adjust delay for smoother rendering
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private Node createCandleStick(OHLCData ohlcData) {
        double openValue = ohlcData.getOpen();
        double highValue = ohlcData.getHigh();
        double lowValue = ohlcData.getLow();
        double closeValue = ohlcData.getClose();

        double candleWidth = calculateCandleWidth();
        double candleHeight = calculateCandleHeight(openValue, closeValue);
        double bodyY = Math.min(yAxis.getDisplayPosition(openValue), yAxis.getDisplayPosition(closeValue));

        double upperWickStartY = yAxis.getDisplayPosition(highValue);
        double lowerWickEndY = yAxis.getDisplayPosition(lowValue);

        Rectangle body = new Rectangle();
        body.setWidth(candleWidth);
        body.setHeight(candleHeight);
        body.setFill(closeValue >= openValue ? Color.GREEN : Color.RED);
        body.setY(bodyY);
        Line upperWick = new Line(candleWidth / 2, upperWickStartY, candleWidth / 2, bodyY);
        upperWick.setStroke(closeValue >= openValue ? Color.GREEN : Color.RED);

        Line lowerWick = new Line(candleWidth / 2, bodyY + candleHeight, candleWidth / 2, lowerWickEndY);
        lowerWick.setStroke(closeValue >= openValue ? Color.GREEN : Color.RED);

        Group candlestick = new Group(body, upperWick, lowerWick);
        candlestick.setLayoutX(getXAxis().getDisplayPosition(ohlcData.getDateTime()) - candleWidth / 2);
        //candlestick.setLayoutY(getYAxis().getDisplayPosition(Math.max(openValue, closeValue)));
        Tooltip tooltip = new Tooltip(
                "DateTime: " + ohlcData.getDateTime() + "\n" +
                        "Open: " + ohlcData.getOpen() + "\n" +
                        "High: " + ohlcData.getHigh() + "\n" +
                        "Low: " + ohlcData.getLow() + "\n" +
                        "Close: " + ohlcData.getClose()
        );
        Tooltip.install(candlestick, tooltip);

        return candlestick;
    }
    // Method to get the current chart data
    public List<XYChart.Series<LocalDateTime, Double>> getChartData() {
        return getData();
    }

    public LocalDateTime[] getDataMinMax() {
        LocalDateTime minDate = LocalDateTime.MAX;
        LocalDateTime maxDate = LocalDateTime.MIN;
        for (XYChart.Series<LocalDateTime, Double> series : getData()) {
            for (XYChart.Data<LocalDateTime, Double> y : series.getData()) {
                if (y.getXValue().isBefore(minDate)) {
                    minDate = y.getXValue();
                }
                if (y.getXValue().isAfter(maxDate)) {
                    maxDate = y.getXValue();
                }
            }
        
        }
        return new LocalDateTime[] { minDate, maxDate };

    }

    public Double[] getDataMinMaxy() {
        Double minval = Double.MAX_VALUE;
        Double maxval = Double.MIN_VALUE;
        for (XYChart.Series<LocalDateTime, Double> series : getData()) {
            for (XYChart.Data<LocalDateTime, Double> y : series.getData()) {
                if (y.getYValue() < minval) {
                    minval = y.getYValue();
                }

                if (y.getYValue() > maxval) {
                    maxval = y.getYValue();
                }
            }
        
        }
        return new Double[] { minval, maxval };

    }
    private double calculateCandleWidth() {
        if (ohlcDataList.size() < 2) {
            return 10; // Default width
        }

        double axisLength = xAxis.getWidth();
        double numDataPoints = ohlcDataList.size();
        double candleWidthFraction = 0.2; // Space allocation per candle

        double calculatedWidth = (axisLength / numDataPoints) * candleWidthFraction;
        return Math.max(1.5, calculatedWidth); // Ensure minimum width
    }

    private double calculateCandleHeight(Double o, Double c) {
        return (Math.abs(yAxis.getDisplayPosition(o) - yAxis.getDisplayPosition(c)));
    }

    @Override
    protected void updateAxisRange() {
        xAxis.invalidateRangeInternal(getDataMinMax());
        yAxis.invalidateRangeInternal(getDataMinMaxy());
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