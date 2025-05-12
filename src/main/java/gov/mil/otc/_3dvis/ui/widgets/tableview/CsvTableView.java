package gov.mil.otc._3dvis.ui.widgets.tableview;

import gov.mil.otc._3dvis.ui.widgets.TextWithStyleClass;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CsvTableView {

    public static TableView<String[]> create(File file) {
        CsvTableView csvTableView = new CsvTableView();
        return csvTableView.createTableView(file);
    }

    private CsvTableView() {
    }

    private TableView<String[]> createTableView(File file) {
        TableView<String[]> tableView = new TableView<>();
        String[] columnNames = null;
        List<String[]> rows = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
            String header = bufferedReader.readLine();
            header = header.replace("\"", "");
            columnNames = header.split(",");

            while (true) {
                String[] row = readRow(bufferedReader, columnNames.length);
                if (row.length == 0) {
                    break;
                } else {
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            String message = String.format("CsvTableView::createTableView:Error processing file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }

        if (columnNames != null) {
            for (String columnName : columnNames) {
                TableColumn<String[], String> column = new TableColumn<>(columnName);
                tableView.getColumns().add(column);

                column.setCellValueFactory(cellDataFeatures -> {
                    String[] cells = cellDataFeatures.getValue();
                    int columnIndex = cellDataFeatures.getTableView().getColumns().indexOf(cellDataFeatures.getTableColumn());
                    if (columnIndex >= cells.length) {
                        return new SimpleStringProperty("");
                    } else {
                        return new SimpleStringProperty(cells[columnIndex]);
                    }
                });

                column.setCellFactory(param -> {
                    TableCell<String[], String> cell = new TableCell<>();
                    Text text = new TextWithStyleClass();
                    cell.setGraphic(text);
                    cell.setPrefHeight(Region.USE_COMPUTED_SIZE);
                    text.wrappingWidthProperty().bind(column.widthProperty());
                    text.textProperty().bind(cell.itemProperty());
                    return cell;
                });

                tableView.setItems(FXCollections.observableArrayList(rows));
            }
        }

        return tableView;
    }

    private String[] readRow(BufferedReader bufferedReader, int numberOfColumns) {
        String[] row = new String[numberOfColumns];
        for (int i = 0; i < numberOfColumns; i++) {
            String value = getNextValue(bufferedReader);
            if (value == null) {
                return new String[0];
            } else {
                row[i] = value;
            }
        }
        return row;
    }

    private String getNextValue(BufferedReader bufferedReader) {
        StringBuilder value = new StringBuilder();
        Character character;
        do {
            character = getNextCharacter(bufferedReader);
            if (character == null) {
                return null;
            } else if (character == '\n' || character == ',') {
                return "";
            }
        } while (character != '"');

        boolean quotesFound = false;
        while (true) {
            character = getNextCharacter(bufferedReader);
            if (character == null) {
                return null;
            }
            if (quotesFound) {
                if (character == '"') {
                    value.append(character);
                    quotesFound = false;
                } else {
                    break;
                }
            } else {
                if (character == '"') {
                    quotesFound = true;
                } else {
                    value.append(character);
                }
            }
        }
        while (character != null && character != ',' && character != '\n') {
            character = getNextCharacter(bufferedReader);
        }
        return value.toString();
    }

    private Character getNextCharacter(BufferedReader bufferedReader) {
        try {
            int intValue = bufferedReader.read();
            if (intValue >= 0) {
                return (char) intValue;
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "CsvTableView::getNextCharacter", e);
        }
        return null;
    }
}
