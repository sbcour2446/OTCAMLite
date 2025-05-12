package gov.mil.otc._3dvis.data.ulog;

import java.nio.ByteBuffer;

public class FieldFormat {
    public final String name;
    public final String type;
    public final int size; //array length (-1 if not an array)

    public FieldFormat(String formatStr) {
        String[] p = formatStr.split(" ");
        name = p[1];
        if (p[0].contains("[")) {
            // Array
            String[] q = p[0].split("\\[");
            type = q[0];
            size = Integer.parseInt(q[1].split("\\]")[0]);
        } else {
            type = p[0];
            size = -1;
        }
    }

    public FieldFormat(String name, String type, int size) {
        this.name = name;
        this.type = type;
        this.size = size;
    }

    public String getFullTypeString() {
        String size_str = (size >= 0) ? ("[" + size + "]") : "";
        return type + size_str;

    }

    boolean isArray() {
        return size >= 0 && !"char".equals(type);
    }

    public Object getValue(ByteBuffer buffer) {
        Object v;
        if (size >= 0) {
            if (type.equals("char")) {
                byte[] stringBytes = new byte[size];
                buffer.get(stringBytes);
                String s = new String(stringBytes);
                int end = s.indexOf('\0');
                if (end < 0) {
                    v = s;
                } else {
                    v = s.substring(0, end);
                }
            } else {
                Object[] arr = new Object[size];
                for (int i = 0; i < size; i++) {
                    arr[i] = getSingleValue(buffer);
                }
                v = arr;
            }
        } else {
            v = getSingleValue(buffer);
        }
        return v;
    }

    private Object getSingleValue(ByteBuffer buffer) {
        return switch (type) {
            case "float" -> buffer.getFloat();
            case "double" -> buffer.getDouble();
            case "int8_t", "bool" -> (int) buffer.get();
            case "uint8_t" -> buffer.get() & 0xFF;
            case "int16_t" -> (int) buffer.getShort();
            case "uint16_t" -> buffer.getShort() & 0xFFFF;
            case "int32_t" -> buffer.getInt();
            case "uint32_t" -> buffer.getInt() & 0xFFFFFFFFL;
            case "int64_t", "uint64_t" -> buffer.getLong();
            case "char" -> buffer.get();
            default -> throw new RuntimeException("Unsupported type: " + type);
        };
    }

    public String toString() {
        return String.format("%s %s", getFullTypeString(), name);
    }
}
