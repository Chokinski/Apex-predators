
package com.jat;

import javafx.collections.FXCollections;

import javafx.geometry.Side;
import javafx.scene.chart.ValueAxis;

import javafx.util.StringConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.text.DecimalFormat;

/**
 * @author Aidan Korczynski
 * The CurrencyAxis class extends the ValueAxis<Double> to create a custom axis for displaying currency values.
 * It supports custom tick label formatting and multi-threaded tick value calculation.
 * 
 * <p>Constructor:</p>
 * <ul>
 * <li>{@link #CurrencyAxis(double, double, OHLCChart)}: Initializes the axis with specified lower and upper bounds and a reference to the chart.</li>
 * </ul>
 * <p>Features:</p>
 * <ul>
 * <li>Custom tick label formatting based on the range of values.</li>
 * <li>Multi-threaded calculation of tick positions for performance optimization.</li>
 * <li>Support for both horizontal and vertical axes.</li>
 * </ul>
 */
public class CurrencyAxis extends ValueAxis<Double> {

    
    public Range range;
    private int MAX_TICK_COUNT = 7; // Adjust as needed

    public CurrencyAxis(double lb, double ub, OHLCChart c) {
        
        setAutoRanging(false);
        setSide(Side.RIGHT);
        setTickLabelsVisible(true);
        setTickLabelGap(0);
        setAnimated(false);
        
        MAX_TICK_COUNT = 20;
        this.range = new Range(lb, ub);
        

        // Set the formatter for tick labels
        setTickLabelFormatter(new StringConverter<Double>() {
            
            @Override
            public String toString(Double value) {
                if (value == null) {
                    return "";
                }

                double val = value;
                double range = ub - lb;
                DecimalFormat df;

                if (range > 1_000_000) {
                    df = new DecimalFormat("$0.##M");
                } else if (range > 1000) {
                    df = new DecimalFormat("$0.##K");
                } else {
                    df = new DecimalFormat("$0.##");
                }

                return df.format(val / (range > 1_000_000 ? 1_000_000 : (range > 1000 ? 1000 : 1)));
            }

            @Override
            public Double fromString(String string) {
                return null; // Not needed for formatting
            }
        });
        this.getStyleClass().add("axis-currency");
    }




    public static class Range {
        double lowerBound;
        double upperBound;

        Range(double lowerBound, double upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }
    }

    public void setBounds(double lowerBound, double upperBound) {
        this.range = new Range(lowerBound, upperBound);
        invalidateRange();
        requestAxisLayout();

    }

    @Override
    protected Range getRange() {
        return range;
    }

    

    @Override
    protected List<Double> calculateTickValues(double length, Object range) {
        if (range == null || !(range instanceof Range)) {
            System.out.println("Range is not of the correct type or null...");
            return FXCollections.observableArrayList();
        }
    
        Range r = (Range) range;
        double rangeInValue = r.upperBound - r.lowerBound;
        double tickInterval = Math.max(1, rangeInValue / (MAX_TICK_COUNT - 1));
    
        return calculateTickPositions(r.lowerBound, r.upperBound, tickInterval);
    }

    private List<Double> calculateTickPositions(double lower, double upper, double tickInterval) {
        // Determine the total number of tick values
        long totalTicks = (long) Math.ceil((upper - lower) / tickInterval);
    
        // Create a thread pool
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    
        // Create futures for each tick
        List<CompletableFuture<List<Double>>> futures = new ArrayList<>();
        long chunkSize = 1000; // Number of ticks to process per thread
    
        for (long i = 0; i < totalTicks; i += chunkSize) {
            long startIndex = i;
            long endIndex = Math.min(i + chunkSize, totalTicks);
    
            CompletableFuture<List<Double>> future = CompletableFuture.supplyAsync(() -> {
                List<Double> tickValues = new ArrayList<>();
                for (long j = startIndex; j < endIndex; j++) {
                    double tickValue = lower + j * tickInterval;
                    tickValue = Math.round(tickValue * 1e9) / 1e9; // Rounding to avoid floating-point inaccuracies
                    if (tickValue <= upper) {
                        tickValues.add(tickValue);
                    }
                }
                return tickValues;
            }, executor);
    
            futures.add(future);
        }
    
        // Combine results from all futures
        List<Double> tickValues = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    
        // Shutdown the executor service
        executor.shutdown();
    
        return tickValues;
    }
    @Override
    public String getTickMarkLabel(Double number) {
        if (number == null) {
            ////System.out.println("Number is null, returning empty label");
            return "";
        }

        double range = this.range.upperBound - this.range.lowerBound;
        DecimalFormat df;

        if (range > 1_000_000) {
            df = new DecimalFormat("$0.##M");
        } else if (range > 1000) {
            df = new DecimalFormat("$0.##K");
        } else {
            df = new DecimalFormat("$0.##");
        }

        String label = df.format(number / (range > 1_000_000 ? 1_000_000 : (range > 1000 ? 1000 : 1)));
        //System.out.println("Formatted label for " + number + ": " + label);
        return label;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        //System.out.println("Layout children called");

        // Fetch the tick marks
        
        //System.out.println("Tick marks: " + tickMarks);

        // No need to loop through tick marks and set labels anymore
        // Labels will be automatically managed by the formatter set above
    }

@Override
public Double getValueForDisplay(double displayPosition) {
    double range = this.range.upperBound - this.range.lowerBound;
    if (range == 0) {
        return this.range.lowerBound;
    }

    double axisLength = getSide().isHorizontal() ? getWidth() : getHeight();

    // Ensure the axis length is not zero (prevents division by zero)
    if (axisLength == 0) {
        return this.range.lowerBound;
    }

    // Adjust displayPosition for vertical axis (invert Y-coordinates)
    if (getSide().isVertical()) {
        displayPosition = (axisLength - displayPosition) + getInsets().getTop();
    }

    return this.range.lowerBound + (displayPosition / axisLength) * range;
}

@Override
public double getDisplayPosition(Double value) {
    double range = this.range.upperBound - this.range.lowerBound;
    if (range == 0) return 0;

    double axisLength = getSide().isHorizontal() ? getWidth() : getHeight();
    if (axisLength == 0) return 0; // Prevent division by zero

    double position = (value - this.range.lowerBound) / range * axisLength;

    // ✅ Ensure correct inversion for vertical axes
    if (getSide().isVertical()) {
        position = axisLength - position; // Invert so larger values go UP
    }

    return position;
}


public double getCandlePos(Double value) {
    double range = this.range.upperBound - this.range.lowerBound;
    if (range == 0) return 0;

    double axisLength = getSide().isHorizontal() ? getWidth() : getHeight();
    if (axisLength == 0) return 0; // Prevent division by zero

    // Calculate the base position without offset
    double position = (value - this.range.lowerBound) / range * axisLength;

    // Add your manual offset (can be positive or negative)
    double offset = -37.5; // Example offset value, adjust as needed
    position += offset;

    // ✅ Ensure correct inversion for vertical axes
    if (getSide().isVertical()) {
        position = axisLength - position; // Invert so larger values go UP
    }

    return position;
}



    @Override
    protected void setRange(Object range, boolean animate) {
        if (range instanceof Range) {
            Range r = (Range) range;
            setBounds(r.lowerBound, r.upperBound);
        } else {
            throw new IllegalArgumentException("Unsupported range object type: " + (range != null ? range.getClass().getName() : "null"));
        }
    }

    @Override
    protected List<Double> calculateMinorTickMarks() {
        List<Double> minorTickMarks = new ArrayList<>();

        // Ensure we have a valid range
        if (getRange() == null || !(getRange() instanceof Range)) {
            //System.out.println("Invalid range object type: " + (getRange() != null ? getRange().getClass().getName() : "null"));
            return minorTickMarks; // Return empty list or handle as needed
        }

        Range range = (Range) getRange();
        double lowerBound = range.lowerBound;
        double upperBound = range.upperBound;

        // Calculate minor tick interval (e.g., 10 minor ticks between major ticks)
        int minorTickCount = 10; // Adjust as needed
        double majorTickInterval = (upperBound - lowerBound) / (getTickMarks().size() - 1); // Calculate major tick interval
        double minorTickInterval = majorTickInterval / (minorTickCount + 1); // Calculate minor tick interval

        // Generate minor tick marks
        double minorTick = lowerBound + minorTickInterval;
        while (minorTick < upperBound) {
            minorTickMarks.add(minorTick);
            minorTick += minorTickInterval;
        }

        return minorTickMarks;
    }

    public void invalidateRangeInternal(Double[] l) {
        // Validate input
        if (l == null || l.length < 2) {
            return; // Prevent null pointer errors
        }
        else {
    
        // Get min and max values
        double minCurrency = l[0];
        double maxCurrency = l[1];
    
        // Ensure we only update bounds if they actually changed
        if (minCurrency != range.lowerBound || maxCurrency != range.upperBound) {
            setRange(new Range(minCurrency, maxCurrency), true);
            calculateTickValues(MAX_TICK_COUNT, new Range(minCurrency, maxCurrency));
            requestAxisLayout();
        }
    
        setAutoRanging(true);
        }
    }

}