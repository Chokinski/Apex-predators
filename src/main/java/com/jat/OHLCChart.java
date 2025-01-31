package com.jat;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.XYChart;

import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

import java.util.List;

import java.util.concurrent.CompletableFuture;

public class OHLCChart extends XYChart<LocalDateTime, Double> {
    public Canvas canvas;
    private ObservableList<OHLCData> ohlcDataList;
    protected CurrencyAxis yAxis;
    protected DateTimeAxis xAxis;
    public AnchorPane pane;
    public CandleStick activeCandlestick = null; // Track the current candlestick
    private ArrayList<CandleStick> candlesticks = new ArrayList<>();
    public OHLCChart(DateTimeAxis xAxis, CurrencyAxis yAxis, ObservableList<OHLCData> ohlcDataList, AnchorPane pane) {
        super(xAxis, yAxis);
        this.ohlcDataList = ohlcDataList;
        this.yAxis = yAxis;
        this.xAxis = xAxis;
        this.pane = pane;
        this.pane.getChildren().clear();
        setTitle("OHLC Chart");
        xAxis.setLabel("Time");
        yAxis.setLabel("Price");
        setData(FXCollections.observableArrayList());
        
        // Initial rendering setup
        Series<LocalDateTime, Double> series = new Series<>();
        getData().add(series);

        Platform.runLater(() -> {
            this.canvas = new Canvas(pane.getWidth(), pane.getHeight());
            this.canvas.widthProperty().bind(pane.widthProperty());
            this.canvas.heightProperty().bind(pane.heightProperty());
            this.canvas.setMouseTransparent(true);
            this.getChildren().add(this.canvas);
            this.setOnScroll(event -> {
                double deltaY = event.getDeltaY();
                if (event.isControlDown()) {

                    // Zoom on the vertical (y-axis)
                    double lowerY = yAxis.range.lowerBound;
                    double upperY = yAxis.range.upperBound;
                    double yAxisRange = upperY - lowerY;
                    double zoomFactor = event.getDeltaY() > 0 ? 0.9 : 1.1; // Zoom in/out
                    double newRange = yAxisRange * zoomFactor;
                    double midpoint = lowerY + yAxisRange / 2;

                    double newLowerY = midpoint - newRange / 2;
                    double newUpperY = midpoint + newRange / 2;

                    yAxis.range.lowerBound = newLowerY;
                    yAxis.range.upperBound = newUpperY;
                    yAxis.setBounds(newLowerY, newUpperY);
                    layoutPlotChildren();
                    event.consume();

                }

                else {
                    double lowerEpoch = toEpochMilli(xAxis.range.lowerBound);
                    double upperEpoch = toEpochMilli(xAxis.range.upperBound);
                    double xAxisRange = upperEpoch - lowerEpoch;

                    double zoomFactor = deltaY > 0 ? 0.9 : 1.1; // Zoom in/out
                    double newRange = xAxisRange * zoomFactor;
                    double midpoint = lowerEpoch + xAxisRange / 2;

                    long newLowerEpoch = (long) (midpoint - newRange / 2);
                    long newUpperEpoch = (long) (midpoint + newRange / 2);

                    xAxis.range.lowerBound = LocalDateTime.ofEpochSecond(newLowerEpoch / 1000, 0, ZoneOffset.UTC);
                    xAxis.range.upperBound = LocalDateTime.ofEpochSecond(newUpperEpoch / 1000, 0, ZoneOffset.UTC);

                    xAxis.setBounds(LocalDateTime.ofEpochSecond(newLowerEpoch / 1000, 0, ZoneOffset.UTC),
                            LocalDateTime.ofEpochSecond(newUpperEpoch / 1000, 0, ZoneOffset.UTC));

                    layoutPlotChildren();
                    event.consume();
                }

            });
            addToolTipListener();
        });
        // Incremental rendering
        // renderCandlesticksIncrementally();
    }

    // Convert LocalDateTime to epoch milliseconds
    private double toEpochMilli(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public void addToolTipListener() {

        

        this.setOnMouseMoved(event -> {
            boolean foundCandlestick = false; // Flag to track if we found a candlestick under the mouse
        
            for (CandleStick cstick : candlesticks) {
                double topY = Math.min(cstick.y, cstick.y + cstick.height);
                double bottomY = Math.max(cstick.y, cstick.y + cstick.height);
        
                if (event.getX() >= cstick.x && event.getX() <= cstick.x + cstick.width &&
                    event.getY() >= topY && event.getY() <= bottomY) {
        
                    if (activeCandlestick != cstick) { // Only redraw if it's a different candlestick
                        
                        drawTooltip(this.canvas.getGraphicsContext2D(), cstick.tooltip, event.getX(), event.getY());
                        activeCandlestick = cstick; // Update active tooltip
                    }
        
                    foundCandlestick = true;
                    break; // Stop checking other candlesticks
                }
            }
        
            // If the mouse is not over any candlestick, clear the tooltip
            if (!foundCandlestick && activeCandlestick != null) {
                clearTooltip();
                activeCandlestick = null;
            }

        });
}
    /*
     * TODO: -For dev use, copy template [], allow copilot to check code if its
     * done, if not, copi states [NEED] if finished and states [NEED] copilot will
     * update to [DONE]-
     * 
     * When given data, take a set amount of data points and display them on the
     * chart, percentage perhaps? [DONE]
     * This will be useful for the initial display of the chart, as well as for the
     * slider functionality that will be implemented later on. [DONE]
     * The data will be displayed in the form of candlesticks, with each candlestick
     * representing a set amount of data points. [DONE]
     * The candlesticks will be colored based on the close value of the data point.
     * [DONE]
     * The candlesticks will be displayed in the chart area, with the x-axis
     * representing the time and the y-axis representing the price. [DONE]
     * The chart will be updated with new data points as they are received. [DONE]
     * The chart... able to display a large amount of data points without
     * performance issues. [NEED]
     * The chart... able to display data points in real-time. [NEED]
     * The chart... visually appealing and easy to read.
     * The chart... easy to interact with, such as zooming in and out, panning, and
     * selecting data points. [NEED]
     * The chart... customizable, such as changing the color scheme, the time period
     * displayed, and the type of chart used. [NEED]
     * The chart... responsive to user input, such as resizing the chart area,
     * changing the time period displayed, and selecting data points.
     * 
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

                Series<LocalDateTime, Double> series = new Series<>();
                chunk.forEach(data -> series.getData().add(toChartData(data)));
                Platform.runLater(() -> {
                    getData().add(series);
                });
            }
        });

    }

    private XYChart.Data<LocalDateTime, Double> toChartData(OHLCData ohlcData) {
        return new XYChart.Data<>(ohlcData.getDateTime(), Math.min(yAxis.getDisplayPosition(ohlcData.getOpen()), yAxis.getDisplayPosition(ohlcData.getClose())));
    }

    @Override
    protected void layoutPlotChildren() {
        // Efficient rendering by skipping unnecessary operations
        if (ohlcDataList == null || ohlcDataList.isEmpty() || this.canvas == null) {
            return;
        }
        candlesticks.clear();
        int chunkSize = Math.max(50, Math.min(ohlcDataList.size() / 10, 250));
        Canvas canvas = this.canvas; // Assuming a canvas is available for drawing
        GraphicsContext gc = this.canvas.getGraphicsContext2D();
        CompletableFuture.supplyAsync(() -> {

            int end = 0;
            int start = 0;
            List<List<OHLCData>> chunkedData = new ArrayList<>();
            int totalChunks = (int) Math.ceil((double) ohlcDataList.size() / chunkSize);

            for (int i = 0; i < totalChunks; i++) {
                start = i * chunkSize;
                end = Math.min(start + chunkSize, ohlcDataList.size());
                List<OHLCData> chunk = ohlcDataList.subList(start, end);
                chunkedData.add(chunk);
            }
            return chunkedData;
        }).thenAccept(chunkedData -> {
            Platform.runLater(() -> {

                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Clear previous drawings
                chunkedData.forEach(chunk -> chunk.forEach(ohlcData ->{ 
                    drawCandleStick(gc, ohlcData);
                    
                }));
            });
        }).exceptionally(ex -> {
            ex.printStackTrace(); // Log any errors
            return null;
        });
    }

    public Canvas getCanvas() {
        return this.canvas;
    }

    private void drawCandleStick(GraphicsContext gc, OHLCData ohlcData) {
        // Retrieve values directly from OHLCData
        double open = ohlcData.getOpen();
        double close = ohlcData.getClose();
        double high = ohlcData.getHigh();
        double low = ohlcData.getLow();
        LocalDateTime date = ohlcData.getDateTime();

        // Calculate the height of the candlestick body (difference between open and
        // close)
        double cHeight = calculateCandleHeight(open, close);

        // Determine the Y position for the body (starting point for open/close)
        double bY = Math.min(yAxis.getDisplayPosition(open), yAxis.getDisplayPosition(close));

        // Calculate the Y positions for the upper and lower wicks (high and low)
        double uWickY = yAxis.getDisplayPosition(high);
        double lWickY = yAxis.getDisplayPosition(low);

        // Set the candle width and determine color (green for upward, red for downward)
        double candleWidth = calculateCandleWidth();
        Color candlestickColor = close >= open ? Color.GREEN : Color.RED;

        // Set drawing parameters
        gc.setFill(candlestickColor);
        gc.setStroke(candlestickColor);
        gc.setLineWidth(1);

        // Draw the candle body (rectangular shape)
        double bodyX = getXAxis().getDisplayPosition(date) - candleWidth / 2;
        gc.fillRect(bodyX, bY, candleWidth, cHeight);

        // Draw the upper wick (line from high to the top of the body)
        gc.strokeLine(getXAxis().getDisplayPosition(date), uWickY,
                getXAxis().getDisplayPosition(date), bY);

        // Draw the lower wick (line from low to the bottom of the body)
        gc.strokeLine(getXAxis().getDisplayPosition(date), lWickY,
                getXAxis().getDisplayPosition(date), bY + cHeight);

        // Create a tooltip-like string
        String tooltipText = String.format(
                "DateTime: %s\nOpen: %.2f\nHigh: %.2f\nLow: %.2f\nClose: %.2f",
                date, open, high, low, close);

        // Define the area to check for mouse hover (the area of the candlestick)
        double mouseAreaX = bodyX;
        double mouseAreaY = bY;
        double mouseAreaWidth = candleWidth;
        double mouseAreaHeight = cHeight + (uWickY - lWickY);
        candlesticks.add(new CandleStick(mouseAreaX, mouseAreaY, mouseAreaWidth, mouseAreaHeight,tooltipText));

        // Add mouse event listeners to show the tooltip
        
    }

    private void clearTooltip() {
        Platform.runLater(() -> {
            GraphicsContext gc = this.canvas.getGraphicsContext2D();
            //gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight()); // Clear everything
            layoutPlotChildren(); // Redraw the candlestick chart
        });
    }

    // Helper method to draw a tooltip on the canvas
    private void drawTooltip(GraphicsContext gc, String text, double x, double y) {

        Platform.runLater(() -> {

            gc.setFill(Color.color(0, 0, 0, 0.8));
        gc.fillRect(x + 10, y + 10, 150, 70); // Draw the background for the tooltip
        gc.setFill(Color.WHITE);
        gc.fillText(text, x + 15, y + 25); // Draw the tooltip text
        
    });
        
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
                // Add padding (2% of the range) to avoid clipping
                double range = maxval - minval;
                double padding = range * 0.02; // 2% padding
        return new Double[] {minval, maxval};

    }




    private double calculateCandleWidth() {
        if (ohlcDataList.size() < 2) {
            return 10; // Default width
        }

        double axisLength = xAxis.getWidth();
        double numDataPoints = ohlcDataList.size();

        double calculatedWidth = axisLength / (numDataPoints + 2); // Adjust for margins
        return Math.max(2.5, Math.min(calculatedWidth, 8)); // Set sensible min/max limits
    }

    private double calculateCandleHeight(Double o, Double c) {
        return (Math.abs(yAxis.getDisplayPosition(o) - yAxis.getDisplayPosition(c)));
    }

    @Override
    protected void updateAxisRange() {
        //xAxis.invalidateRangeInternal(getDataMinMax());
    }

    @Override
    protected void dataItemAdded(Series<LocalDateTime, Double> series, int itemIndex,
            Data<LocalDateTime, Double> item) {
        // Update the plot to reflect the new data item
        // layoutPlotChildren();
    }

    @Override
    protected void dataItemChanged(Data<LocalDateTime, Double> item) {
        // Update the plot to reflect the changed data item

    }

    @Override
    protected void dataItemRemoved(Data<LocalDateTime, Double> item, Series<LocalDateTime, Double> series) {
        // Update the plot to reflect the removed data item

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
    public class CandleStick {
        public double x=0.0;
        public double y=0.0;
        public double width=0.0;
        public double height=0.0;
        public String tooltip="";
    
        public CandleStick(double x, double y, double width, double height,String tooltip) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.tooltip = tooltip;
        }
    
    
    
    
    }

}

