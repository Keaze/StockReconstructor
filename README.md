# Stock Reconstructor

Rebuilds stock records from a stock CSV and a stock history CSV. The default entry point is a terminal UI (Lanterna)
that lets you load the files, optionally set a date cutoff, inspect the reconstructed stocks, and export results.

## Requirements

- Java 17+
- Gradle (wrapper included)

## Build

```bash
./gradlew build
```

## Run

### TUI (default)

```bash
./gradlew run
```

TUI flow:

- Enter paths for `stock.csv` and `stockhistory.csv`
- Optional date in `YYYY-MM-DD` (blank means no cutoff)
- View reconstructed stocks in a scrollable table
- Press Enter on a stock to view applied movements
- Export results to a directory

Shortcuts:

- Input screen: `Alt+R` run, `Alt+Q` quit
- Results: `Enter` movements, `Alt+S` focus search, `Alt+F` filter, `Alt+C` clear, `Alt+E` export, `Alt+R` errors,
  `Alt+Q` close
- Movements/Errors: `Alt+Q` close

### Batch mode (legacy)

```bash
./gradlew run --args="--batch"
```

Batch mode uses the hardcoded CSV filenames in `Main` and writes results to `./results/`.

## CSV compatibility

The CSV files remain in their original German format. Parsing uses the existing column order and event codes (e.g.
`BEWGZU`, `MGKOAB`) and maps them internally to English identifiers.

## Tests

```bash
./gradlew test
```
