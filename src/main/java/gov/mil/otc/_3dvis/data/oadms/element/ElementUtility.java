package gov.mil.otc._3dvis.data.oadms.element;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ElementUtility {

    public static String parseString(Node node) {
        Node child = node.getFirstChild();
        return child != null ? child.getNodeValue() : "";
    }

    public static boolean parseBoolean(Node node) {
        Node child = node.getFirstChild();
        if (child != null) {
            try {
                return Boolean.parseBoolean(child.getNodeValue());
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "ElementUtility::parseBoolean", e);
            }
        }
        return false;
    }

    public static Integer parseInteger(Node node) {
        return parseInteger(node, null);
    }

    public static Integer parseInteger(Node node, Integer defaultValue) {
        Node child = node.getFirstChild();
        if (child != null) {
            try {
                return Integer.parseInt(child.getNodeValue());
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "ElementUtility::parseLong", e);
            }
        }
        return defaultValue;
    }

    public static Long parseLong(Node node) {
        return parseLong(node, null);
    }

    public static Long parseLong(Node node, Long defaultValue) {
        Node child = node.getFirstChild();
        if (child != null) {
            try {
                return Long.parseLong(child.getNodeValue());
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "ElementUtility::parseLong", e);
            }
        }
        return defaultValue;
    }

    public static Double parseDouble(Node node) {
        return parseDouble(node, null);
    }

    public static Double parseDouble(Node node, Double defaultValue) {
        Node child = node.getFirstChild();
        if (child != null) {
            try {
                return Double.parseDouble(child.getNodeValue());
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "ElementUtility::parseDouble", e);
            }
        }
        return defaultValue;
    }

    public static long parseSystemTime(Node node) {
        String timestampString = parseString(node);
        if (timestampString != null && !timestampString.isBlank()) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(timestampString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
                Instant instant = localDateTime.atZone(ZoneId.of("UTC")).toInstant();
                return instant.toEpochMilli();
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "ElementUtility::parseSystemTime", e);
            }
        }
        return 0;
    }

    @SuppressWarnings("rawtypes")
    public static void printAllElements(Document doc) {
        Element element = doc.getDocumentElement();
        Map<String, Map> elementTree = getChildElementTree(element);
        printTree(elementTree, 0);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void printTree(Map<String, Map> tree, int level) {
        String spacer = "";
        for (int i = 0; i < level; i++) {
            spacer += "    ";
        }
        for (Map.Entry<String, Map> entry : tree.entrySet()) {
            System.out.println(spacer + entry.getKey());
            if (entry.getValue() != null) {
                printTree(entry.getValue(), level + 1);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map<String, Map> getChildElementTree(Node node) {
        Map<String, Map> elementTree = new TreeMap<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            Map<String, Map> childElementTree = getChildElementTree(child);
            if (!elementTree.containsKey(child.getNodeName())) {
                elementTree.put(child.getNodeName(), childElementTree);
            } else {
                mergeTrees(elementTree.get(child.getNodeName()), childElementTree);
            }
        }
        return elementTree;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void mergeTrees(Map<String, Map> tree1, Map<String, Map> tree2) {
        for (Map.Entry<String, Map> entry : tree2.entrySet()) {
            if (!tree1.containsKey(entry.getKey())) {
                tree1.put(entry.getKey(), entry.getValue());
            } else {
                mergeTrees(tree1.get(entry.getKey()), entry.getValue());
            }
        }
    }

    private ElementUtility() {
    }
}
