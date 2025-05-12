package gov.mil.otc._3dvis.ui.widgets.treeview;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import javafx.util.Duration;

import static javafx.animation.Animation.INDEFINITE;

/**
 * An abstract factory that automatically creates auto scrolling when items are dragged.
 *
 * @param <T> The type populating the tree view items.
 */
public abstract class AutoScrollTreeViewCellFactory<T> implements Callback<TreeView<T>, TreeCell<T>> {
    /**
     * The timeline to scroll the tree view
     */
    private final Timeline scrollTimeLine = new Timeline();

    /**
     * Integer representing the scroll direction.
     */
    private double scrollDirection = 0;

    /**
     * The representative tree
     */
    private final TreeView<T> tree;

    /**
     * Constructor.
     *
     * @param treeView The tree view who will be populated by the calls of this factory.
     */
    protected AutoScrollTreeViewCellFactory(TreeView<T> treeView) {
        this.tree = treeView;
        setupScrolling();
    }

    /**
     * The method that must be overridden by the child of this class to be invoked by the call() method of the callback
     * factory.
     *
     * @param param The tree view.
     * @return A created tree cell.
     */
    public abstract TreeCell<T> doCall(TreeView<T> param);

    /**
     * {@inheritDoc}
     */
    @Override
    public TreeCell<T> call(TreeView<T> param) {
        return doCall(param);
    }

    /**
     * Configures auto scrolling of the tree view up and down when an item is dragged north or south of the visual
     * bounds of the tree view, within the tolerance of the minimum and maximum X visual bounds components of the tree view/
     */
    private void setupScrolling() {
        scrollTimeLine.setCycleCount(INDEFINITE);
        scrollTimeLine.getKeyFrames().add(new KeyFrame(Duration.millis(20), "Scoll", actionEvent -> dragScroll()));
        tree.setOnDragExited(event -> {
            if (event.getX() >= tree.getLayoutBounds().getMinX() && event.getX() <= tree.getLayoutBounds().getMaxX()) {
                if (event.getY() > 0) {
                    scrollDirection = 1.0 / tree.getExpandedItemCount();
                } else {
                    scrollDirection = -1.0 / tree.getExpandedItemCount();
                }
                scrollTimeLine.play();
            } else {
                scrollTimeLine.stop();
            }
        });
        tree.setOnDragEntered(event -> scrollTimeLine.stop());
        tree.setOnDragDone(event -> scrollTimeLine.stop());

    }

    /**
     * Scrolls the view.
     */
    private void dragScroll() {
        ScrollBar sb = getVerticalScrollbar();
        if (sb != null) {
            double newValue = sb.getValue() + scrollDirection;
            newValue = Math.min(newValue, 1.0);
            newValue = Math.max(newValue, 0.0);
            sb.setValue(newValue);
        }
    }

    /**
     * Gets the vertical scrollbar from the root node to invoke scrolling behavior on.
     *
     * @return The vertical scrollbar from the root note.
     */
    private ScrollBar getVerticalScrollbar() {
        ScrollBar result = null;
        for (Node n : tree.lookupAll(".scroll-bar")) {
            if (n instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) n;
                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
                    result = bar;
                }
            }
        }
        return result;
    }
}
