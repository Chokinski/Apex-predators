# CTFXPlots+
![MIT License](https://img.shields.io/badge/License-MIT-green.svg)

<img src="CTFXPlots+ Demo.png" alt="Demo" width="600"/>
CTFXPlots+ is a custom JavaFX library designed for seamless and performant trading chart integration. Built with efficiency in mind, this library leverages optimized rendering techniques to ensure smooth charting experiences for financial applications.

## Features
- **High-Performance OHLC Charts**: Leverages JavaFX Canvas for efficient rendering of candlestick charts.
- **Dynamic Data Integration**: Easily integrates OHLC (Open, High, Low, Close) data for financial charting.
- **Scalable & Interactive**: Supports zooming, panning, and tooltips for enhanced user interaction.
- **Customizable & Extensible**: Designed to accommodate additional chart types and user-defined styles in future updates.
- **Seamless JavaFX Integration**: Works smoothly within JavaFX applications without performance bottlenecks.

## Current Components
### 1. PlotHandler
The `PlotHandler` class is responsible for initializing and managing the `OHLCChart` within a JavaFX application. It provides:
- Automatically parsing data for the minimum and maximum date and price values from OHLC data.
- Dynamic updates for axis ranges to accommodate incoming data.
- Seamless integration of the `OHLCChart` into a JavaFX `AnchorPane` and `ScrollPane`.
- Support for adjustable viewport sizes and automatic resizing.

### 2. OHLCChart
The `OHLCChart` class is a custom JavaFX chart built for financial data visualization. It extends the `XYChart` class, utilizing:
- **X-Axis**: LocalDateTime representation for time-based data.
- **Y-Axis**: Double values for financial price movements.
- **Canvas-based Rendering**: Optimized drawing for smooth performance, even with large datasets.
- **Interactive Features**: Supports zooming, panning, and real-time updates.

## Installation
To use **CTFXPlots+** in your JavaFX project:

1. Clone the repository:
   ```sh
   git clone https://github.com/Chokinski/CTFXPlotsPlus.git
   ```
2. Add the library to your JavaFX project:
   - If using Maven, add dependencies (to be defined in future versions).
   - If using manually, include the compiled JAR file in your classpath.

## Usage Example
```java
PlotHandler plotHandler = new PlotHandler();
ObservableList<OHLCData> ohlcDataList = FXCollections.observableArrayList();
ScrollPane scrollPane = new ScrollPane();
AnchorPane chartPane = new AnchorPane();
plotHandler.showOHLCChart(scrollPane, chartPane, true, 50, ohlcDataList);
```

## Roadmap
- [ ] Additional Chart Types (Line Charts, Volume Charts, multi-dimensional data visuals etc.)
- [ ] Tailored Customization (Themes, Color Schemes, Indicators)

## License
CTFXPlots+ is released under the MIT License.

## Contributing
I'm a solo developer. Contributions are more than welcome! If you’d like to contribute, please fork the repository and submit a pull request.

## Contact
For questions or support, reach out via GitHub Issues or email `aidankorczynskibusy@gmail.com`.

##

