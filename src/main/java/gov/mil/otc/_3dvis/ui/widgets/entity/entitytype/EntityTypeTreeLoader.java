package gov.mil.otc._3dvis.ui.widgets.entity.entitytype;

import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.datamodel.EntityTypeUtility;
import javafx.scene.control.TreeItem;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityTypeTreeLoader {

    private static final EntityTypeTreeLoader SINGLETON = new EntityTypeTreeLoader();
    private final TreeItem<EntityTypeItem> treeItem = new TreeItem<>();
    private final Thread loadingThread;
    private boolean isLoaded = false;

    /**
     * The constructor.
     */
    private EntityTypeTreeLoader() {
        loadingThread = new Thread(() -> {
            createEntityTypeTreeItem();
            isLoaded = true;
        }, "MunitionsTreeLoader Thread");
    }

    /**
     * Start loading tree nodes.
     */
    public static void start() {
        if (!SINGLETON.isLoaded) {
            SINGLETON.loadingThread.start();
        }
    }

    /**
     * Waits for the load to complete.
     */
    public static void waitForComplete() {
        if (!SINGLETON.isLoaded) {
            try {
                SINGLETON.loadingThread.join();
            } catch (InterruptedException e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public static TreeItem<EntityTypeItem> getTreeItem() {
        return SINGLETON.treeItem;
    }

    private void createEntityTypeTreeItem() {
        for (Map.Entry<Integer, String> kindEntry : EntityTypeUtility.getKinds().entrySet()) {

            TreeItem<EntityTypeItem> kindTreeItem = createKindTreeItem(kindEntry.getKey(), kindEntry.getValue());

            if (kindTreeItem == null) {
                return;
            }

            treeItem.getChildren().add(kindTreeItem);
        }
    }

    private TreeItem<EntityTypeItem> createKindTreeItem(int kind, String description) {
        if (EntityTypeUtility.isShutdown()) {
            return null;
        }

        TreeItem<EntityTypeItem> kindTreeItem = new TreeItem<>(new EntityTypeItem(
                String.format("%d (%s)", kind, description)));

        for (Map.Entry<Integer, String> domainEntry : EntityTypeUtility.getDomainsWithQualified(kind).entrySet()) {

            TreeItem<EntityTypeItem> domainTreeItem =
                    createDomainTreeItem(kind, domainEntry.getKey(), domainEntry.getValue());

            if (domainTreeItem == null) {
                return null;
            }

            kindTreeItem.getChildren().add(domainTreeItem);
        }

        return kindTreeItem;
    }

    private TreeItem<EntityTypeItem> createDomainTreeItem(int kind, int domain, String description) {
        if (EntityTypeUtility.isShutdown()) {
            return null;
        }

        TreeItem<EntityTypeItem> domainTreeItem = new TreeItem<>(new EntityTypeItem(
                String.format("%d.%d (%s)", kind, domain, description)));

        for (Map.Entry<Integer, String> categoryEntry
                : EntityTypeUtility.getCategories(kind, domain).entrySet()) {

            TreeItem<EntityTypeItem> categoryTreeItem = createCategoryTreeItem(kind, domain,
                    categoryEntry.getKey(), categoryEntry.getValue());

            if (categoryTreeItem == null) {
                return null;
            }

            domainTreeItem.getChildren().add(categoryTreeItem);
        }

        return domainTreeItem;
    }

    private TreeItem<EntityTypeItem> createCategoryTreeItem(int kind, int domain, int category, String description) {
        if (EntityTypeUtility.isShutdown()) {
            return null;
        }

        TreeItem<EntityTypeItem> categoryTreeItem = new TreeItem<>(new EntityTypeItem(
                String.format("%d.%d.*.%d (%s)", kind, domain, category, description)));

        for (Map.Entry<Integer, String> countryEntry
                : EntityTypeUtility.getCountriesWithQualified(kind, domain, category, true).entrySet()) {

            TreeItem<EntityTypeItem> countryTreeItem = createCountryTreeItem(kind, domain, category,
                    countryEntry.getKey(), countryEntry.getValue());

            if (countryTreeItem == null) {
                return null;
            }

            categoryTreeItem.getChildren().add(countryTreeItem);
        }

        return categoryTreeItem;
    }

    private TreeItem<EntityTypeItem> createCountryTreeItem(int kind, int domain, int category, int country, String description) {
        if (EntityTypeUtility.isShutdown()) {
            return null;
        }

        TreeItem<EntityTypeItem> countryTreeItem = new TreeItem<>(new EntityTypeItem(
                String.format("%d (%s)", country, description)));

        for (Map.Entry<EntityType, String> entityType :
                EntityTypeUtility.getQualifiedEntityTypes(kind, domain, country, category).entrySet()) {

            TreeItem<EntityTypeItem> entityTypeItemTreeItem =
                    new TreeItem<>(new EntityTypeItem(entityType.getKey(), entityType.getValue()));
            countryTreeItem.getChildren().add(entityTypeItemTreeItem);
        }

        return countryTreeItem;
    }
}
