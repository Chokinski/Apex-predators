
package com.jat.ctfxplotsplus;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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


    public int MAX_TICK_COUNT = 100;
    public Range range;
    public ObservableList<OHLCData> dataset;
    public List<LocalDateTime> tickMarks;
    public List<LocalDateTime> tickValues = new ArrayList<>();
    public DateTimeAxis(LocalDateTime lowerBound, LocalDateTime upperBound, ObservableList<OHLCData> dataset) {
        // this(lowerBound, upperBound, chart);
        setAutoRanging(false); // Disable auto-ranging
        setSide(Side.BOTTOM);
        setTickLabelsVisible(true);
        setTickLabelGap(0);
        setAnimated(false);
        this.getStyleClass().add("axis-datetime");
        this.tickLengthProperty().set(1);
        this.tickMarkVisibleProperty().set(false);
        
        this.dataset=dataset;
        if (dataset.size() < MAX_TICK_COUNT) {
            MAX_TICK_COUNT = dataset.size();
        }
        //this.chart = chart; // Store reference to the chart
        this.range = new Range(lowerBound, upperBound);
        setRange(new Range(lowerBound, upperBound), true);
        //System.out.println("Info:" + isTickLabelsVisible() + " " + isTickMarkVisible() + " " + isAutoRanging());
        this.tickMarks =supplyTickValues(getRange());
        this.tickValues = supplyTickValues(getRange());
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
            //System.out.println("Setting range: " + r.lowerBound + " to " + r.upperBound);
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
public void updateMarks(){
try {
    this.tickMarks =supplyTickValues(getRange());
    this.tickValues = supplyTickValues(getRange());
}
catch (Exception e){
    System.out.println("Reached end of data set.");}
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
    
        if (this.dataset == null || this.dataset.isEmpty()) {
            System.out.println("OHLC Data List is empty or not initialized.");
            return List.of();
        }
        
        List<LocalDateTime> tickValues = new ArrayList<>();
        this.dataset.sort((a, b) -> a.getDateTime().compareTo(b.getDateTime()));
        int startIndex = Math.max(0, this.dataset.size() - MAX_TICK_COUNT);
        for (int i = startIndex; i < this.dataset.size(); i++) {
            if ((i - startIndex) % 5 == 0) { // Add every 5th tick mark
                tickValues.add(this.dataset.get(i).getDateTime());
            }
        }
    
        tickValues.sort(LocalDateTime::compareTo); // Ensure tick marks are in chronological order
        return tickValues;
    }
    
    protected List<LocalDateTime> supplyTickValues(Object range) {
        if (!(range instanceof Range)) {
            System.out.println("Range is not of type Range");
            return List.of(); // Return an empty list if range is invalid
        }
    
        Range r = (Range) range;
    
        if (this.dataset == null || this.dataset == null || this.dataset.isEmpty()) {
            System.out.println("OHLC Data List is empty or not initialized.");
            return List.of();
        }
    
        List<LocalDateTime> tickValues = new ArrayList<>();
        this.dataset.sort((a, b) -> a.getDateTime().compareTo(b.getDateTime()));
        for (int i = this.dataset.size() - MAX_TICK_COUNT; i != this.dataset.size(); i++){
            tickValues.add(this.dataset.get(i).getDateTime());
        }
        LocalDateTime max = LocalDateTime.MIN;
        LocalDateTime min = LocalDateTime.MAX;
        for (LocalDateTime data : tickValues){
            if (data.isBefore(min)){
                min = data;
            }
            if (data.isAfter(max)){
                max = data;
            }

        }
        setRange(new Range(min,max), false);
        tickValues.sort(LocalDateTime::compareTo); // Ensure tick marks are in chronological order
        //System.out.println("Tickvaluesize = " + tickValues.size());
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
    public ObservableList<TickMark<LocalDateTime>> getTickMarks() {
        ObservableList<TickMark<LocalDateTime>> tickMarks = FXCollections.observableArrayList();
        
        if (range == null || range.lowerBound == null || range.upperBound == null) {
            return FXCollections.observableArrayList(); // Return an empty list if range is invalid
        }
    
        for (int i = 0; i < this.tickValues.size(); i++) {
            if (MAX_TICK_COUNT > 20 && MAX_TICK_COUNT < 50) {

                if (i % 5 == 0) { // Add every 5th tick mark
                    
                    LocalDateTime tickValue = this.tickValues.get(i);
                    double position = getDisplayPosition(tickValue);
                    TickMark<LocalDateTime> tickMark = new TickMark<>();
                    tickMark.setValue(tickValue);
                    tickMark.setPosition(position);
                    
                    //System.out.println("Generated tick mark: " + tickValue + " at position: " + position);
                    tickMarks.add(tickMark);
                }


            }
            else if(MAX_TICK_COUNT < 20&& MAX_TICK_COUNT < 100 && MAX_TICK_COUNT > 5){
                
                if (i % 2 == 0) { // Add every 5th tick mark
                    
                    LocalDateTime tickValue = this.tickValues.get(i);
                    double position = getDisplayPosition(tickValue);
                    TickMark<LocalDateTime> tickMark = new TickMark<>();
                    tickMark.setValue(tickValue);
                    tickMark.setPosition(position);
                    
                    //System.out.println("Generated tick mark: " + tickValue + " at position: " + position);
                    tickMarks.add(tickMark);
                }
            }
            else if (50<MAX_TICK_COUNT && MAX_TICK_COUNT < 100) {
                
                if (i % 10 == 0) { // Add every 5th tick mark
                    
                    LocalDateTime tickValue = this.tickValues.get(i);
                    double position = getDisplayPosition(tickValue);
                    TickMark<LocalDateTime> tickMark = new TickMark<>();
                    tickMark.setValue(tickValue);
                    tickMark.setPosition(position);
                    
                    //System.out.println("Generated tick mark: " + tickValue + " at position: " + position);
                    tickMarks.add(tickMark);
                }


            }

            else if (MAX_TICK_COUNT > 100&& MAX_TICK_COUNT < 200) {
                
                if (i % 20 == 0) { // Add every 5th tick mark
                    
                    LocalDateTime tickValue = this.tickValues.get(i);
                    double position = getDisplayPosition(tickValue);
                    TickMark<LocalDateTime> tickMark = new TickMark<>();
                    tickMark.setValue(tickValue);
                    tickMark.setPosition(position);
                    
                    //System.out.println("Generated tick mark: " + tickValue + " at position: " + position);
                    tickMarks.add(tickMark);
                }


            }
            else if (MAX_TICK_COUNT > 200 && MAX_TICK_COUNT < 500) {
                
                if (i % 50 == 0) { // Add every 5th tick mark
                    
                    LocalDateTime tickValue = this.tickValues.get(i);
                    double position = getDisplayPosition(tickValue);
                    TickMark<LocalDateTime> tickMark = new TickMark<>();
                    tickMark.setValue(tickValue);
                    tickMark.setPosition(position);
                    
                    //System.out.println("Generated tick mark: " + tickValue + " at position: " + position);
                    tickMarks.add(tickMark);
                }


            }

            else if (MAX_TICK_COUNT > 500) {
                
                if (i % 100 == 0) { // Add every 5th tick mark
                    
                    LocalDateTime tickValue = this.tickValues.get(i);
                    double position = getDisplayPosition(tickValue);
                    TickMark<LocalDateTime> tickMark = new TickMark<>();
                    tickMark.setValue(tickValue);
                    tickMark.setPosition(position);
                    
                    //System.out.println("Generated tick mark: " + tickValue + " at position: " + position);
                    tickMarks.add(tickMark);
                }


            }
            else if (tickMarks.size() >= MAX_TICK_COUNT) {
                break; // Stop if we have enough tick marks
            }
            else if ( MAX_TICK_COUNT < 5 || MAX_TICK_COUNT == 5) {
                LocalDateTime tickValue = this.tickValues.get(i);
                double position = getDisplayPosition(tickValue);
                TickMark<LocalDateTime> tickMark = new TickMark<>();
                tickMark.setValue(tickValue);
                tickMark.setPosition(position);
                
                //System.out.println("Generated tick mark: " + tickValue + " at position: " + position);
                tickMarks.add(tickMark);


            }

        }
        return tickMarks;
    }

    @Override
    public double getDisplayPosition(LocalDateTime dateTime) {
        if (range == null || range.lowerBound == null || range.upperBound == null) {
            return 0;
        }
        

        long totalRangeMinutes = range.lowerBound.until(range.upperBound, ChronoUnit.MINUTES);
        if (totalRangeMinutes <= 0) return 0; // Prevent division by zero
    
        double axisLength = getSide().isHorizontal() ? getWidth() : getHeight();
        long elapsedMinutes = range.lowerBound.until(dateTime, ChronoUnit.MINUTES);
        
        double position = (elapsedMinutes / (double) totalRangeMinutes) * axisLength;
    
        //System.out.println("Calculated display position for " + dateTime + ": " + position);
        return position;
    }



    public double getCandlePos(LocalDateTime dateTime) {
        if (range == null || range.lowerBound == null || range.upperBound == null) {
            return 0;
        }
    
        // Fetch tick values once instead of calling supplyTickValues repeate
        for (LocalDateTime tickMark : this.tickMarks) {
            if (tickMark.equals(dateTime)) {
                return getDisplayPosition(tickMark) + 15;
            }
        }
        //this.tickMarks.add(dateTime);
        return 0.0;
    }

@Override
public LocalDateTime getValueForDisplay(double displayPosition) {
    if (range == null || range.lowerBound == null || range.upperBound == null) {
        return null;
    }

    List<TickMark<LocalDateTime>> tickMarks = getTickMarks();
    TickMark<LocalDateTime> closestTick = null;
    double minDistance = Double.MAX_VALUE;

    for (TickMark<LocalDateTime> tickMark : tickMarks) {
        double distance = Math.abs(tickMark.getPosition() - displayPosition);
        if (distance < minDistance) {
            minDistance = distance;
            closestTick = tickMark;
        }
    }

    return closestTick != null ? closestTick.getValue() : range.lowerBound;
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