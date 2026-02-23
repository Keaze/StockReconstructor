package com.app.tui;

import com.app.history.model.MovementRecord;
import com.app.history.reader.CsvMovementReader;
import com.app.history.reader.MovementReader;
import com.app.history.reader.MovementStream;
import com.app.stock.StockData;
import com.app.stock.model.StockRecord;
import com.app.stock.reader.CsvStockIO;
import com.app.stock.reader.StockIO;
import com.app.utils.Result;
import com.app.utils.StockError;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class StockReconstructorTui {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public void start() {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        try {
            Screen screen = terminalFactory.createScreen();
            screen.startScreen();
            MultiWindowTextGUI gui = new MultiWindowTextGUI(screen);

            TextBox stockPath = new TextBox().setPreferredSize(new TerminalSize(60, 1));
            TextBox movementPath = new TextBox().setPreferredSize(new TerminalSize(60, 1));
            TextBox dateText = new TextBox().setPreferredSize(new TerminalSize(20, 1));
            BasicWindow window = new BasicWindow("Stock Reconstruction") {
                @Override
                public boolean handleInput(KeyStroke key) {
                    if (key.getKeyType() == KeyType.Character && key.isAltDown()) {
                        char keyChar = Character.toLowerCase(key.getCharacter());
                        if (keyChar == 'r') {
                            runReconstruction(gui, stockPath, movementPath, dateText);
                            return true;
                        }
                        if (keyChar == 'q') {
                            close();
                            return true;
                        }
                    }
                    return super.handleInput(key);
                }
            };
            window.setComponent(buildInputPanel(gui, window, stockPath, movementPath, dateText));
            window.setHints(java.util.List.of(Window.Hint.CENTERED));
            window.setCloseWindowWithEscape(true);
            gui.addWindowAndWait(window);
            screen.stopScreen();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize TUI", e);
        }
    }

    private Panel buildInputPanel(MultiWindowTextGUI gui, BasicWindow window, TextBox stockPath, TextBox movementPath, TextBox dateText) {
        Panel panel = new Panel(new GridLayout(2).setHorizontalSpacing(1));
        panel.addComponent(new Label("Stock CSV"));
        panel.addComponent(stockPath);

        panel.addComponent(new Label("Stock History CSV"));
        panel.addComponent(movementPath);

        panel.addComponent(new Label("Date (YYYY-MM-DD)"));
        panel.addComponent(dateText);

        Panel actions = new Panel(new LinearLayout(Direction.HORIZONTAL));
        actions.addComponent(new Button("Run", () -> runReconstruction(gui, stockPath, movementPath, dateText)));
        actions.addComponent(new Button("Quit", window::close));
        panel.addComponent(new Label(""));
        panel.addComponent(actions);
        panel.addComponent(new Label(""));
        panel.addComponent(new Label("Shortcuts: Alt+R=Run, Alt+Q=Quit"));

        return panel;
    }

    private void runReconstruction(MultiWindowTextGUI gui, TextBox stockPathBox, TextBox movementPathBox, TextBox dateBox) {
        String stockPath = stockPathBox.getText().trim();
        String movementPath = movementPathBox.getText().trim();
        if (stockPath.isEmpty() || movementPath.isEmpty()) {
            MessageDialog.showMessageDialog(gui, "Missing Input", "Please enter both CSV paths.");
            return;
        }
        if (!Files.exists(Path.of(stockPath))) {
            MessageDialog.showMessageDialog(gui, "Invalid Path", "Stock CSV file not found: " + stockPath);
            return;
        }
        if (!Files.exists(Path.of(movementPath))) {
            MessageDialog.showMessageDialog(gui, "Invalid Path", "Stock history CSV file not found: " + movementPath);
            return;
        }

        LocalDate stockDate = null;
        String dateValue = dateBox.getText().trim();
        if (!dateValue.isEmpty()) {
            try {
                stockDate = LocalDate.parse(dateValue, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                MessageDialog.showMessageDialog(gui, "Invalid Date", "Please use YYYY-MM-DD format.");
                return;
            }
        }

        Result<StockData, StockError> stockResult = readStocks(stockPath, stockDate);
        if (stockResult.isFailure()) {
            MessageDialog.showMessageDialog(gui, "Stock Read Error", stockResult.error().message());
            return;
        }

        StockData stockData = stockResult.getOrThrow();
        LocalDate finalStockDate = stockDate;
        Map<Integer, List<MovementRecord>> appliedMovements = new HashMap<>();
        Set<Integer> finalizedStocks = new HashSet<>();
        Result<MovementStream, StockError> movementResult = readMovements(movementPath);
        if (movementResult.isFailure()) {
            MessageDialog.showMessageDialog(gui, "Movement Read Error", movementResult.error().message());
            return;
        }

        try (MovementStream stream = movementResult.getOrThrow()) {
            stream.stream().forEach(movement -> {
                if (movement != null && movement.isSuccessful()) {
                    MovementRecord record = movement.getOrThrow();
                    if (isMovementApplied(record, finalStockDate, finalizedStocks)) {
                        appliedMovements.computeIfAbsent(record.stockNumber(), ignored -> new ArrayList<>()).add(record);
                    }
                }
                stockData.handleMovement(movement);
            });
        }
        stockData.cleanUp();

        showResults(gui, stockPath, stockData, appliedMovements);
    }

    private Result<StockData, StockError> readStocks(String stockPath, LocalDate stockDate) {
        StockIO stockReader = new CsvStockIO(stockPath);
        Result<StockData, StockError> result = stockReader.readStocks();
        if (result.isFailure() || stockDate == null) {
            return result;
        }
        StockData original = result.getOrThrow();
        return Result.success(new StockData(original.getStockRecords(), stockDate));
    }

    private Result<MovementStream, StockError> readMovements(String movementPath) {
        MovementReader movementReader = new CsvMovementReader(movementPath);
        return movementReader.readMovements();
    }

    private boolean isMovementApplied(MovementRecord record, LocalDate stockDate, Set<Integer> finalizedStocks) {
        if (record == null) {
            return false;
        }
        if (stockDate != null && record.date() != null && record.date().isBefore(stockDate)
                && !finalizedStocks.contains(record.stockNumber())) {
            finalizedStocks.add(record.stockNumber());
            return false;
        }
        return true;
    }

    private void showResults(MultiWindowTextGUI gui, String stockPath, StockData stockData, Map<Integer, List<MovementRecord>> appliedMovements) {
        TextBox searchBox = new TextBox().setPreferredSize(new TerminalSize(40, 1));
        Label countLabel = new Label("");
        Table<String> table = buildTable();
        searchBox.setTextChangeListener((newText, changedByUser) -> applyFilter(stockData, table, countLabel, newText));
        table.setSelectAction(() -> showMovements(gui, table, appliedMovements));
        BasicWindow resultsWindow = new BasicWindow("Stock Results") {
            @Override
            public boolean handleInput(KeyStroke key) {
                if (key.getKeyType() == KeyType.Character && key.isAltDown()) {
                    char keyChar = Character.toLowerCase(key.getCharacter());
                    if (keyChar == 'e') {
                        exportStocks(gui, stockPath, stockData);
                        return true;
                    }
                    if (keyChar == 's') {
                        searchBox.takeFocus();
                        return true;
                    }
                    if (keyChar == 'f') {
                        applyFilter(stockData, table, countLabel, searchBox.getText());
                        return true;
                    }
                    if (keyChar == 'c') {
                        searchBox.setText("");
                        applyFilter(stockData, table, countLabel, "");
                        return true;
                    }
                    if (keyChar == 'r') {
                        showErrors(gui, stockData);
                        return true;
                    }
                    if (keyChar == 'q') {
                        close();
                        return true;
                    }
                }
                return super.handleInput(key);
            }
        };
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        applyFilter(stockData, table, countLabel, "");
        panel.addComponent(countLabel);
        panel.addComponent(new Label("Errors: " + stockData.getErrors().size() + ", critical: " + stockData.isCriticalErrors()));
        panel.addComponent(buildSearchPanel(searchBox, table, countLabel, stockData));
        panel.addComponent(table.withBorder(Borders.singleLine("Stocks")));
        panel.addComponent(new Label("Enter=Movements, Alt+E=Export, Alt+S=Search, Alt+F=Filter, Alt+C=Clear, Alt+R=Errors, Alt+Q=Close"));

        Panel actions = new Panel(new LinearLayout(Direction.HORIZONTAL));
        actions.addComponent(new Button("Export", () -> exportStocks(gui, stockPath, stockData)));
        actions.addComponent(new Button("Errors", () -> showErrors(gui, stockData)));
        actions.addComponent(new Button("Close", resultsWindow::close));
        panel.addComponent(actions);

        resultsWindow.setComponent(panel);
        resultsWindow.setHints(java.util.List.of(Window.Hint.FULL_SCREEN));
        resultsWindow.setCloseWindowWithEscape(true);
        gui.addWindowAndWait(resultsWindow);
    }

    private Table<String> buildTable() {
        Table<String> table = new Table<>("SEQ", "ITEM_NUMBER", "QUANTITY", "LOCATION", "HANDLING_UNIT", "BATCH1");
        table.setSelectAction(() -> {
        });
        table.setCellSelection(true);
        table.setVisibleRows(20);
        return table;
    }

    private Component buildSearchPanel(TextBox searchBox, Table<String> table, Label countLabel, StockData stockData) {
        Panel panel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        panel.addComponent(new Label("Search"));
        panel.addComponent(searchBox);
        panel.addComponent(new Button("Filter", () -> applyFilter(stockData, table, countLabel, searchBox.getText())));
        panel.addComponent(new Button("Clear", () -> {
            searchBox.setText("");
            applyFilter(stockData, table, countLabel, "");
        }));
        return panel;
    }

    private void applyFilter(StockData stockData, Table<String> table, Label countLabel, String filter) {
        String normalized = filter == null ? "" : filter.trim().toLowerCase();
        var model = table.getTableModel();
        model.clear();

        long total = stockData.getStockRecords().size();
        long filtered = stockData.getStockRecords().values().stream()
                .filter(record -> matchesFilter(record, normalized))
                .sorted(Comparator.comparing(StockRecord::getSequenceNumber, Comparator.nullsLast(Integer::compareTo)))
                .peek(record -> model.addRow(
                        formatInt(record.getSequenceNumber()),
                        formatString(record.getItemNumber()),
                        formatDecimal(record.getQuantityOnHand()),
                        formatString(record.getLocation()),
                        formatString(record.getHandlingUnitNumber()),
                        formatString(record.getBatch1())
                ))
                .count();

        if (normalized.isEmpty()) {
            countLabel.setText("Records: " + total);
        } else {
            countLabel.setText("Records: " + filtered + " (filtered from " + total + ")");
        }
    }

    private boolean matchesFilter(StockRecord record, String normalized) {
        if (normalized == null || normalized.isEmpty()) {
            return true;
        }
        String haystack = String.join(" ",
                formatInt(record.getSequenceNumber()),
                formatString(record.getItemNumber()),
                formatDecimal(record.getQuantityOnHand()),
                formatString(record.getLocation()),
                formatString(record.getHandlingUnitNumber()),
                formatString(record.getBatch1())
        ).toLowerCase();
        return haystack.contains(normalized);
    }

    private void showMovements(MultiWindowTextGUI gui, Table<String> table, Map<Integer, List<MovementRecord>> appliedMovements) {
        if (table.getTableModel().getRowCount() == 0) {
            MessageDialog.showMessageDialog(gui, "Movements", "No stock selected.");
            return;
        }
        List<String> row = table.getTableModel().getRow(table.getSelectedRow());
        Integer stockNumber = parseInteger(row.isEmpty() ? null : row.get(0));
        if (stockNumber == null) {
            MessageDialog.showMessageDialog(gui, "Movements", "Unable to read selected stock.");
            return;
        }
        List<MovementRecord> movements = appliedMovements.get(stockNumber);
        if (movements == null || movements.isEmpty()) {
            MessageDialog.showMessageDialog(gui, "Movements", "No applied movements for stock " + stockNumber + ".");
            return;
        }

        BasicWindow movementWindow = new BasicWindow("Movements for " + stockNumber) {
            @Override
            public boolean handleInput(KeyStroke key) {
                if (key.getKeyType() == KeyType.Character && key.isAltDown()
                        && Character.toLowerCase(key.getCharacter()) == 'q') {
                    close();
                    return true;
                }
                return super.handleInput(key);
            }
        };
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        panel.addComponent(new Label("Applied movements: " + movements.size()));

        Table<String> movementTable = new Table<>(
                "SEQ",
                "EVENT",
                "DATE",
                "TIME",
                "QUANTITY_CHANGE",
                "QUANTITY_TOTAL",
                "LOCATION",
                "HANDLING_UNIT",
                "BATCH1",
                "USER"
        );
        movements.forEach(record -> movementTable.getTableModel().addRow(
                formatInt(record.sequenceNumber()),
                record.event() == null ? "" : record.event().name(),
                record.date() == null ? "" : record.date().toString(),
                formatString(record.time()),
                formatDecimal(record.quantityChange()),
                formatDecimal(record.quantityTotal()),
                formatString(record.location()),
                formatString(record.handlingUnitNumber()),
                formatString(record.batch1()),
                formatString(record.user())
        ));
        movementTable.setVisibleRows(20);
        panel.addComponent(movementTable.withBorder(Borders.singleLine("Movements")));
        panel.addComponent(new Label("Shortcuts: Alt+Q=Close"));
        panel.addComponent(new Button("Close", movementWindow::close));
        movementWindow.setComponent(panel);
        movementWindow.setHints(java.util.List.of(Window.Hint.FULL_SCREEN));
        movementWindow.setCloseWindowWithEscape(true);
        gui.addWindowAndWait(movementWindow);
    }

    private void showErrors(MultiWindowTextGUI gui, StockData stockData) {
        if (stockData.getErrors().isEmpty()) {
            MessageDialog.showMessageDialog(gui, "Errors", "No errors recorded.");
            return;
        }

        BasicWindow errorWindow = new BasicWindow("Errors") {
            @Override
            public boolean handleInput(KeyStroke key) {
                if (key.getKeyType() == KeyType.Character && key.isAltDown()
                        && Character.toLowerCase(key.getCharacter()) == 'q') {
                    close();
                    return true;
                }
                return super.handleInput(key);
            }
        };
        Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
        Table<String> table = new Table<>("Errors");
        stockData.getErrors().forEach(error -> table.getTableModel().addRow(error.type() + ": " + error.message()));
        table.setVisibleRows(20);
        panel.addComponent(table.withBorder(Borders.singleLine("Errors")));
        panel.addComponent(new Button("Close", errorWindow::close));
        errorWindow.setComponent(panel);
        errorWindow.setHints(java.util.List.of(Window.Hint.CENTERED));
        errorWindow.setCloseWindowWithEscape(true);
        gui.addWindowAndWait(errorWindow);
    }

    private void exportStocks(MultiWindowTextGUI gui, String stockPath, StockData stockData) {
        String outputDir = TextInputDialog.showDialog(gui, "Export", "Output directory", "results");
        if (outputDir == null || outputDir.trim().isEmpty()) {
            return;
        }
        CsvStockIO stockIO = new CsvStockIO(stockPath);
        Result<Void, StockError> result = stockIO.writeStocks(stockData, outputDir.trim());
        if (result.isFailure()) {
            MessageDialog.showMessageDialog(gui, "Export Failed", result.error().message());
            return;
        }
        MessageDialog.showMessageDialog(gui, "Export Complete", "Exported to " + outputDir.trim());
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatString(String value) {
        return value == null ? "" : value;
    }

    private String formatInt(Integer value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String formatDecimal(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }
}
