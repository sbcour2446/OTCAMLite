package gov.mil.otc._3dvis.ui.projects.nbcrv;

import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.collections.ObservableList;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.LinkedHashSet;

public class TableViewExtra<T> {
    private final TableView<T> tableView;
    private final LinkedHashSet<TableRow<T>> rows = new LinkedHashSet<>();
    private int firstVisibleIndex;
    private int lastVisibleIndex;

    public TableViewExtra(TableView<T> tableView) {
        this.tableView = tableView;
    }

    public void setRowFactory() {
        // Callback to monitor row creation and to identify visible screen rows
        final Callback<TableView<T>, TableRow<T>> rf = tableView.getRowFactory();

        final Callback<TableView<T>, TableRow<T>> modifiedRowFactory = param -> {
            TableRow<T> r = rf != null ? rf.call(param) : new TableRow<>();
            // Save row, this implementation relies on JaxaFX re-using TableRow efficiently
            rows.add(r);
            return r;
        };
        tableView.setRowFactory(modifiedRowFactory);
    }

    /**
     * Changes the current view to ensure that one of the passed index positions
     * is visible on screen. The view is not changed if any of the passed index positions is already visible.
     * The table scroll position is moved so that the closest index to the current position is visible.
     *
     * @param firstIndex First index in scrollTo range
     * @param lastIndex  Last index in scrollTo range
     */
    public void scrollToIndex(int firstIndex, int lastIndex) {
        recomputeVisibleIndexes();
        int visibleIndexSize = lastVisibleIndex - firstVisibleIndex - 1;
        int where = -1;

        if (visibleIndexSize < (lastIndex - firstIndex) || firstIndex < firstVisibleIndex) {
            where = firstIndex;
        } else if (lastIndex > lastVisibleIndex) {
            where = lastIndex - visibleIndexSize;
        }

        if (where > 0) {
            tableView.scrollTo(where);
        }
    }

    private static int closestTo(int[] indices, int value) {
        int x = indices[0];
        int diff = Math.abs(value - x);
        int newDiff;
        for (int v : indices) {
            newDiff = Math.abs(value - v);
            if (newDiff < diff) {
                x = v;
                diff = newDiff;
            }
        }
        return x;
    }

    private void recomputeVisibleIndexes() {
        firstVisibleIndex = -1;
        lastVisibleIndex = -1;

        // Work out which of the rows are visible
        double tableViewHeight = tableView.getHeight();
        double headerHeight = tableView.lookup(".column-header-background").getBoundsInLocal().getHeight();
        double scrollbarHeight = tableView.lookup(".scroll-bar").getBoundsInLocal().getWidth();
        double viewPortHeight = tableViewHeight - headerHeight - scrollbarHeight - UiConstants.SPACING;
        for (TableRow<T> row : rows) {
            if (!row.isVisible()) continue;

            double minY = row.getBoundsInParent().getMinY();
            double maxY = row.getBoundsInParent().getMaxY();

            boolean hidden = (maxY < 0) || (minY > viewPortHeight);
            if (!hidden) {
                if (firstVisibleIndex < 0 || row.getIndex() < firstVisibleIndex) {
                    firstVisibleIndex = row.getIndex();
                }
                if (lastVisibleIndex < 0 || row.getIndex() > lastVisibleIndex) {
                    lastVisibleIndex = row.getIndex();
                }
            }
        }

    }

    /**
     * Find the first row in the tableView which is visible on the display
     *
     * @return -1 if none visible or the index of the first visible row (wholly or fully)
     */
    public int getFirstVisibleIndex() {
        recomputeVisibleIndexes();
        return firstVisibleIndex;
    }

    /**
     * Find the last row in the tableView which is visible on the display
     *
     * @return -1 if none visible or the index of the last visible row (wholly or fully)
     */
    public int getLastVisibleIndex() {
        recomputeVisibleIndexes();
        return lastVisibleIndex;
    }

    /**
     * Ensure that some part of the current selection is visible in the display view
     */
    public void scrollToSelection() {
        ObservableList<Integer> selectedIndices = tableView.getSelectionModel().getSelectedIndices();
        int firstIndex = Integer.MAX_VALUE;
        int lastIndex = Integer.MIN_VALUE;
        for (int index : selectedIndices) {
            if (index < firstIndex) {
                firstIndex = index;
            }
            if (index > lastIndex) {
                lastIndex = index;
            }
        }
        scrollToIndex(firstIndex, lastIndex);
    }
}
