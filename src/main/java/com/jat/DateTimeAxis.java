
package com.jat;

import javafx.geometry.Side;
import javafx.scene.chart.Axis;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
/**
 * 
 * 
 * A custom axis for displaying date and time values in a chart.
 * This class extends the {@link javafx.scene.chart.Axis} class and provides
 * functionality for handling {@link java.time.LocalDateTime} values.
 * 
 * <p>Features include:
 * <ul>
 * <li>Setting custom bounds for the axis</li>
 * <li>Calculating tick values asynchronously</li>
 * <li>Formatting tick mark labels based on the range</li>
 * <li>Handling display positions and value conversions</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 * {@code
 * DateTimeAxis dateTimeAxis = new DateTimeAxis(lowerBound, upperBound, chart);
 * }
 * </pre>
 * 
 * <p>Note: This class is designed to work with the OHLCChart.
 * 
 * @author Aidan Korczynski
 */
public class DateTimeAxis extends Axis<LocalDateTime> {


    private int MAX_TICK_COUNT = 20;
    public Range range;

    public DateTimeAxis(LocalDateTime lowerBound, LocalDateTime upperBound, OHLCChart chart) {
        // this(lowerBound, upperBound, chart);
        setAutoRanging(false); // Disable auto-ranging
        setSide(Side.BOTTOM);
        setTickLabelsVisible(true);
        setTickLabelGap(5);
        setAnimated(false);
        this.getStyleClass().add("axis-datetime");
        this.tickLengthProperty().set(50);

        //this.chart = chart; // Store reference to the chart
        this.range = new Range(lowerBound, upperBound);
    }

     public class Range {
        LocalDateTime lowerBound;
        LocalDateTime upperBound;

        Range(LocalDateTime lowerBound, LocalDateTime upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }
    }

    @Override
    protected void setRange(Object range, boolean animate) {
        if (range instanceof Range) {
            Range r = (Range) range;
            // System.out.println("Setting DateTimeAxis range: " + r.lowerBound + " to " +
            // r.upperBound);
            setBounds(r.lowerBound, r.upperBound);
        } else {
            throw new IllegalArgumentException("Expected range of type Range");
        }
    }

    public void setBounds(LocalDateTime lowerBound, LocalDateTime upperBound) {
        // System.out.println("Setting DateTimeAxis bounds: " + lowerBound + " to " +
        // upperBound);
        range.lowerBound = lowerBound;
        range.upperBound = upperBound;
        invalidateRange();
        requestAxisLayout();
    }

    @Override
    protected Range getRange() {
        // System.out.println("Getting DateTimeAxis range: " + this.lowerBound + " to "
        // + this.upperBound);
        return range;
    }

@Override
protected List<LocalDateTime> calculateTickValues(double length, Object range) {
    if (!(range instanceof Range)) {
        System.out.println("Range is not of type Range");
        return List.of(); // Return an empty list if range is invalid
    }

    Range r = (Range) range;
    long rangeInMinutes = r.lowerBound.until(r.upperBound, java.time.temporal.ChronoUnit.MINUTES);

    // Calculate the tick interval based on the maximum tick count
    long tickInterval = Math.max(1, rangeInMinutes / (MAX_TICK_COUNT - 1));

    return calculateTickPositionsAsync(r.lowerBound, r.upperBound, tickInterval);
}

private List<LocalDateTime> calculateTickPositionsAsync(LocalDateTime lower, LocalDateTime upper, long tickInterval) {
    // Determine the total number of tick values
    long totalTicks = (long) Math.ceil((double) lower.until(upper, ChronoUnit.MINUTES) / tickInterval);

    // Create a thread pool
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // Create futures for tick generation in chunks
    int chunkSize = 1000; // Number of ticks per thread
    List<CompletableFuture<List<LocalDateTime>>> futures = new ArrayList<>();

    for (long i = 0; i < totalTicks; i += chunkSize) {
        long startIndex = i;
        long endIndex = Math.min(i + chunkSize, totalTicks);

        CompletableFuture<List<LocalDateTime>> future = CompletableFuture.supplyAsync(() -> {
            List<LocalDateTime> tickValues = new ArrayList<>();
            for (long j = startIndex; j < endIndex; j++) {
                LocalDateTime tickValue = lower.plusMinutes(j * tickInterval);
                if (!tickValue.isAfter(upper)) {
                    tickValues.add(tickValue);
                }
            }
            return tickValues;
        }, executor);

        futures.add(future);
    }

    // Combine results from all futures
    List<LocalDateTime> tickValues = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());

    // Shutdown the executor service
    executor.shutdown();

    return tickValues;
}

    @Override
    protected Object autoRange(double length) {
        if (range.lowerBound == null || range.upperBound == null) {
            return new Range(LocalDateTime.now(), LocalDateTime.now());
        }
        return new Range(range.lowerBound, range.upperBound);
    }

    @Override
    public double getZeroPosition() {
        return getDisplayPosition(LocalDateTime.ofEpochSecond(0, 0, java.time.ZoneOffset.UTC)); // Unix epoch
    }

    @Override
    public double getDisplayPosition(LocalDateTime dateTime) {
        long rangeInMinutes = range.lowerBound.until(range.upperBound, java.time.temporal.ChronoUnit.MINUTES);
        if (rangeInMinutes <= 0) {
            return 0; // Avoid division by zero or negative range
        }

        double axisLength = getSide().isHorizontal() ? getWidth() : getHeight();
        long dateTimeOffset = range.lowerBound.until(dateTime, java.time.temporal.ChronoUnit.MINUTES);

        // Calculate the position with respect to the adjusted tick interval
        double position = (axisLength * dateTimeOffset) / rangeInMinutes;

        return position;
    }

    @Override
    public LocalDateTime getValueForDisplay(double displayPosition) {
        long rangeInMinutes = range.lowerBound.until(range.upperBound, java.time.temporal.ChronoUnit.MINUTES);
        if (rangeInMinutes <= 0) {
            return range.lowerBound;
        }

        double axisLength = getSide().isHorizontal() ? getWidth() : getHeight();
        long offset = (long) ((displayPosition * rangeInMinutes) / axisLength);
        System.out.println("Display position: " + displayPosition + ", offset: " + offset);
        return range.lowerBound.plusMinutes(offset);
    }

    @Override
    public boolean isValueOnAxis(LocalDateTime dateTime) {
        return dateTime != null && !dateTime.isBefore(range.lowerBound) && !dateTime.isAfter(range.upperBound);
    }

    @Override
    public double toNumericValue(LocalDateTime dateTime) {
        return range.lowerBound.until(dateTime, java.time.temporal.ChronoUnit.MINUTES);
    }

    @Override
    public LocalDateTime toRealValue(double v) {
        return range.lowerBound.plusMinutes((long) v);
    }

    @Override
    public String getTickMarkLabel(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        long rangeInMinutes = range.lowerBound.until(range.upperBound, java.time.temporal.ChronoUnit.MINUTES);
        if (rangeInMinutes < 1440) { // Less than a day (1440 minutes)
            return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        } else if (rangeInMinutes < 10080) { // Less than a week (10080 minutes)
            return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd HH:mm"));
        } else {
            return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }

    public void invalidateRangeInternal(LocalDateTime[] l) {
        // Use the OHLCChart instance to get the chart data

        LocalDateTime minDateTime = LocalDateTime.MAX;
        LocalDateTime maxDateTime = LocalDateTime.MIN;
        LocalDateTime m = l[0];
        LocalDateTime n = l[1];

        if (m.isBefore(minDateTime)) {
            minDateTime = m;
        }
        if (n.isAfter(maxDateTime)) {
            maxDateTime = n;
        }

        // Set the bounds based on the found min and max LocalDateTime
        if (minDateTime != range.lowerBound || maxDateTime != range.upperBound) {
            setRange(new Range(minDateTime, maxDateTime),true);
            calculateTickValues(MAX_TICK_COUNT, new Range(minDateTime, maxDateTime));
            requestAxisLayout();

        }
        setAutoRanging(true);

    }


}