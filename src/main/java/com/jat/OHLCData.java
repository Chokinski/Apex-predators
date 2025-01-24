package com.jat;

import javafx.scene.chart.XYChart;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class OHLCData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDateTime timestamp;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final double volume;
    private final XYChart.Data<LocalDateTime, Number> data;

    public OHLCData(LocalDateTime timestamp, double open, double high, double low, double close, double volume) {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.data = new XYChart.Data<>(timestamp, getMid()); // y value is the midpoint
    }

    // getters for open, high, low, close, volume, data

    public LocalDateTime getDateTime() {return timestamp;}
    public double getOpen() {return open;}
    public double getHigh() {return high;}
    public double getLow() {return low;}
    public double getClose() {return close;}
    public double getVolume() {return volume;}
    public double getMid() {return (open + close) / 2;}
    public double getDelta() {return close - open;}
    public double getChangeInPips() {return (close - open) * 10000;}
    public double getRange() {return high - low;}
    public boolean isBullish() {return close > open;}
    public boolean isBearish() {return close < open;}
    public String getPriceMovement() {
    return isBullish() ? "Bullish" : isBearish() ? "Bearish" : "Neutral";}
    public double getDeltaPercent() {return ((close - open) / open) * 100;}
    public double getVolumeInThousands() {return volume / 1000;}
    public double getVolumeInMillions() {return volume / 1_000_000;}

    public String getOHLC() {
        return "O:" + open + " H:" + high + " L:" + low + " C:" + close;
    }

    public XYChart.Data<LocalDateTime, Number> getData() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OHLCData ohlcData = (OHLCData) obj;
        return timestamp.equals(ohlcData.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp);
    }

@Override
public String toString() {
    return String.format("OHLCData{timestamp=%s, open=%.3f, high=%.3f, low=%.3f, close=%.3f, volume=%.1f}",
            timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
            open, high, low, close, volume);
}
}