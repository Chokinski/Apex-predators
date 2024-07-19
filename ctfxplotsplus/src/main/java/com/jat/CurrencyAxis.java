package com.jat;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CurrencyAxis extends ValueAxis<Double> {

    private double lowerBound;
    private double upperBound; // Default upper bound
    private OHLCChart chart;

    private int MAX_TICK_COUNT = 5; // Adjust as needed

    public CurrencyAxis() {
        this(0, 100, null);
        setAutoRanging(false);
        setSide(Side.RIGHT);
        setTickLabelsVisible(true);
        setTickLabelGap(5);
        setAnimated(true);
        

        // Set the formatter for tick labels
        setTickLabelFormatter(new StringConverter<Double>() {
            
            @Override
            public String toString(Double value) {
                if (value == null) {
                    return "";
                }

                double val = value;
                double range = upperBound - lowerBound;
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


    public CurrencyAxis(double lowerBound, double upperBound, OHLCChart chart) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.chart = chart; // Store reference to the chart

        // Set the formatter for tick labels
        setTickLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                if (value == null) {
                    return "";
                }

                double val = value;
                double range = upperBound - lowerBound;
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
    }

    public static class Range {
        final double lowerBound;
        final double upperBound;

        Range(double lowerBound, double upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }
    }

    public void setBounds(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        if (chart != null) {
            chart.updateAxisRange();
        }
        requestAxisLayout();
    }

    @Override
    protected Range getRange() {
        return new Range(this.lowerBound, this.upperBound);
    }

    

    @Override
    protected List<Double> calculateTickValues(double length, Object range) {
        if (range == null || !(range instanceof Range)) {
            return FXCollections.observableArrayList();
        }
    
        Range r = (Range) range;
        double rangeInValue = r.upperBound - r.lowerBound;
        double tickInterval = rangeInValue / (MAX_TICK_COUNT - 1);
    
        return calculateTickPositions(r.lowerBound, r.upperBound, tickInterval);
    }

    private List<Double> calculateTickPositions(double lower, double upper, double tickInterval) {
        List<Double> tickValues = new ArrayList<>();
        double tickValue = lower;

        while (tickValue <= upper) {
            tickValues.add(tickValue);
            tickValue += tickInterval;
        }

        return tickValues;
    }

    @Override
    public String getTickMarkLabel(Double number) {
        if (number == null) {
            ////System.out.println("Number is null, returning empty label");
            return "";
        }

        double range = this.upperBound - this.lowerBound;
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
        ObservableList<TickMark<Double>> tickMarks = getTickMarks();
        //System.out.println("Tick marks: " + tickMarks);

        // No need to loop through tick marks and set labels anymore
        // Labels will be automatically managed by the formatter set above
    }

@Override
public Double getValueForDisplay(double displayPosition) {
    double range = upperBound - lowerBound;
    if (range == 0) {
        return lowerBound;
    }

    double axisLength = getSide().isHorizontal() ? getWidth() : getHeight();

    // Invert the position for vertical axis to make larger values go up
    if (getSide().isVertical()) {
        displayPosition = axisLength - displayPosition;
    }

    return lowerBound + (displayPosition / axisLength) * range;
}

@Override
public double getDisplayPosition(Double value) {
    double range = upperBound - lowerBound;
    if (range == 0) {
        return 0;
    }

    double axisLength = getSide().isHorizontal() ? getWidth() : getHeight();
    double position = (value - lowerBound) / range * axisLength;

    // Invert the position for vertical axis to make larger values go up
    if (getSide().isVertical()) {
        position = axisLength - position;
    }

    return position;
}

    private double calculateTickUnit(double lowerBound, double upperBound, double length) {
        double range = upperBound - lowerBound;
        double minimumTickUnit = 1.0;

        double averageTickGap = range / (length / 100);
        double multiplier = Math.pow(10, Math.floor(Math.log10(averageTickGap)));

        for (double tickUnitCandidate : new double[]{1, 2, 5, 10, 20, 50, 100, 200, 500}) {
            double roundedAverageTickGap = tickUnitCandidate * multiplier;
            if (Math.floor(range / roundedAverageTickGap) >= (length / 100)) {
                return roundedAverageTickGap;
            }
        }

        return minimumTickUnit * multiplier;
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

    public void invalidateRangeInternal(OHLCChart chart) {
        this.chart = chart; // Update the chart reference if needed
    
        if (chart == null) {
            return;
        }
    
        double minCurrency = Double.MAX_VALUE;
        double maxCurrency = Double.MIN_VALUE;
    
        List<Series<LocalDateTime, Double>> data = chart.getChartData();
    
        // Iterate through all series and their data points to find min and max currency values
        for (Series<LocalDateTime, Double> series : data) {
            for (Data<LocalDateTime, Double> dataPoint : series.getData()) {
                Double currencyValue = dataPoint.getYValue();
                if (currencyValue != null) {
                    if (currencyValue == null || currencyValue < minCurrency) {
                        minCurrency = currencyValue;
                    }
                    if (currencyValue == null || currencyValue > maxCurrency) {
                        maxCurrency = currencyValue;
                    }
                }
            }
        }
    
        // Only update bounds if they have changed significantly
        if (minCurrency != this.lowerBound || maxCurrency != this.upperBound) {
            setBounds(minCurrency, maxCurrency);
            calculateTickValues(maxCurrency, getRange());
            requestAxisLayout();
        }
        //setAutoRanging(true);
    }
}