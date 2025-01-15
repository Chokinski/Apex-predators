package com.jat;

import javafx.geometry.Side;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DateTimeAxis extends Axis<LocalDateTime> {
    
    private LocalDateTime lowerBound;
    private LocalDateTime upperBound;
    private OHLCChart chart;
    private int MAX_TICK_COUNT = 20;
    private Range range;
    
    public DateTimeAxis(LocalDateTime lowerBound, LocalDateTime upperBound, OHLCChart chart) {
        //this(lowerBound, upperBound, chart);
        setAutoRanging(false); // Disable auto-ranging
        setSide(Side.BOTTOM);
        setTickLabelsVisible(true);
        setTickLabelGap(5);
        setAnimated(false);
        this.getStyleClass().add("axis-datetime");
        this.tickLengthProperty().set(50);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.chart = chart; // Store reference to the chart
        this.range = new Range(lowerBound, upperBound);
    }
    
        static class Range {
            final LocalDateTime lowerBound;
            final LocalDateTime upperBound;
    
            Range(LocalDateTime lowerBound, LocalDateTime upperBound) {
                this.lowerBound = lowerBound;
                this.upperBound = upperBound;
            }
        }
        @Override
        protected void setRange(Object range, boolean animate) {
            if (range instanceof Range) {
                Range r = (Range) range;
                //System.out.println("Setting DateTimeAxis range: " + r.lowerBound + " to " + r.upperBound);
                setBounds(r.lowerBound, r.upperBound);
            } else {
                throw new IllegalArgumentException("Expected range of type Range");
            }
        }
    
        public void setBounds(LocalDateTime lowerBound, LocalDateTime upperBound) {
            //System.out.println("Setting DateTimeAxis bounds: " + lowerBound + " to " + upperBound);
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            requestAxisLayout();
        }

        @Override
        protected Range getRange() {
            //System.out.println("Getting DateTimeAxis range: " + this.lowerBound + " to " + this.upperBound);
            return new Range(lowerBound, upperBound);
        }

    @Override
    protected List<LocalDateTime> calculateTickValues(double length, Object range) {
        //System.out.println("Calculating tick values...");
        if (!(range instanceof Range)) {
            System.out.println("Range is not of type Range");
            return List.of(); // return empty list if range is not of type Range
        }

        Range r = (Range) range;
        long rangeInMinutes = r.lowerBound.until(r.upperBound, java.time.temporal.ChronoUnit.MINUTES);

        // Calculate the tick interval based on the maximum tick count
        long tickInterval = Math.max(1, rangeInMinutes / (MAX_TICK_COUNT - 1));

        return calculateTickPositions(r.lowerBound, r.upperBound, tickInterval);
    }

    private List<LocalDateTime> calculateTickPositions(LocalDateTime lower, LocalDateTime upper, long tickInterval) {
        List<LocalDateTime> tickValues = new ArrayList<>();
        LocalDateTime tickValue = lower;

while (!tickValue.isAfter(upper) && tickValues.size() < MAX_TICK_COUNT) {
    tickValues.add(tickValue);
    tickValue = tickValue.plusMinutes(tickInterval);
}

        return tickValues;
    }

    @Override
    protected Object autoRange(double length) {
        if (lowerBound == null || upperBound == null) {
            return new Range(LocalDateTime.now(), LocalDateTime.now());
        }
        return new Range(lowerBound, upperBound);
    }

    @Override
    public double getZeroPosition() {
        return getDisplayPosition(LocalDateTime.ofEpochSecond(0, 0, java.time.ZoneOffset.UTC)); // Unix epoch
    }

    @Override
    public double getDisplayPosition(LocalDateTime dateTime) {
        long rangeInMinutes = lowerBound.until(upperBound, java.time.temporal.ChronoUnit.MINUTES);
        if (rangeInMinutes <= 0) {
            return 0; // Avoid division by zero or negative range
        }

        double axisLength = getSide().isHorizontal() ? getWidth() : getHeight();
        long dateTimeOffset = lowerBound.until(dateTime, java.time.temporal.ChronoUnit.MINUTES);

        // Calculate the position with respect to the adjusted tick interval
        double position = (axisLength * dateTimeOffset) / rangeInMinutes;

        return position;
    }

    @Override
    public LocalDateTime getValueForDisplay(double displayPosition) {
        long rangeInMinutes = lowerBound.until(upperBound, java.time.temporal.ChronoUnit.MINUTES);
        if (rangeInMinutes <= 0) {
            return lowerBound;
        }

        double axisLength = getSide().isHorizontal() ? getWidth() : getHeight();
        long offset = (long) ((displayPosition * rangeInMinutes) / axisLength);
        System.out.println("Display position: " + displayPosition + ", offset: " + offset);
        return lowerBound.plusMinutes(offset);
    }

    @Override
    public boolean isValueOnAxis(LocalDateTime dateTime) {
        return dateTime != null && !dateTime.isBefore(lowerBound) && !dateTime.isAfter(upperBound);
    }

    @Override
    public double toNumericValue(LocalDateTime dateTime) {
        return lowerBound.until(dateTime, java.time.temporal.ChronoUnit.MINUTES);
    }

    @Override
    public LocalDateTime toRealValue(double v) {
        return lowerBound.plusMinutes((long) v);
    }

    @Override
    public String getTickMarkLabel(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        long rangeInMinutes = lowerBound.until(upperBound, java.time.temporal.ChronoUnit.MINUTES);
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

            
        
    
        //System.out.println("Calculated minDateTime: " + minDateTime + ", maxDateTime: " + maxDateTime);
        // Set the bounds based on the found min and max LocalDateTime
        if (minDateTime!=this.lowerBound || maxDateTime!=this.upperBound) {
            setBounds(minDateTime, maxDateTime);
            calculateTickValues(MAX_TICK_COUNT, range);
            requestAxisLayout();
            
        }
        setAutoRanging(true);
        
    }
    public void giveChart(OHLCChart chart) {
        this.chart = chart;
    }
}