package gov.mil.otc._3dvis.project.dlm;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class that creates a factory structure to serialize and deserialize children of the same parent using
 * the GSON library.
 * @param <T> The parent class type from which children will need to be discerned from.
 */
public final class RuntimeTypeAdapterFactory<T> implements TypeAdapterFactory {
    /**
     * The parent class type.
     */
    private final Class<?> baseType;
    /**
     * The field denoting the type.
     */
    private final String typeFieldName;
    /**
     * A label to subtype class mapping.
     */
    private final Map<String, Class<?>> labelToSubtype = new LinkedHashMap<>();
    /**
     * The subtype to label mapping.
     */
    private final Map<Class<?>, String> subtypeToLabel = new LinkedHashMap<>();

    /**
     * Default constructor.
     * @param baseType The parent class type for which we are registering children.
     * @param typeFieldName The name of the JSON field that will be used to tell the difference between children types.
     * @throws NullPointerException IF the type field name or base type is null.
     */
    private RuntimeTypeAdapterFactory(Class<?> baseType, String typeFieldName) {
        if (typeFieldName == null || baseType == null) {
            throw new NullPointerException();
        }
        this.baseType = baseType;
        this.typeFieldName = typeFieldName;
    }

    /**
     * Creates a new runtime type adapter using for {@link RuntimeTypeAdapterFactory#baseType} using
     * {@link RuntimeTypeAdapterFactory#typeFieldName} as the type field name. Type field names are case sensitive.
     *
     * @param baseType The parent type of the children being registered.
     * @param typeFieldName The "hidden" JSON variable that will be used to discern children types when parsing.
     * @param <T> The Java representation of the parent JSON object from which children will inherit from.
     *
     * @return A new RuntimeAdapterFactory.
     */
    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType, String typeFieldName) {
        return new RuntimeTypeAdapterFactory<>(baseType, typeFieldName);
    }

    /**
     * Creates a new runtime type adapter for {@link RuntimeTypeAdapterFactory#baseType} using "type" as
     * the type field name.
     *
     * @param baseType The parent type of the children being registered.
     * @param <T> The Java representation of the parent JSON object from which children will inherit from.
     *
     * @return A new RuntimeAdapterFactory
     */
    public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType) {
        return new RuntimeTypeAdapterFactory<>(baseType, "type");
    }

    /**
     * Registers type identified by label. Labels are case sensitive.
     *
     * @param type The child type to register.
     * @param label The label that will be used in the JSON to identify a child type from a collection of parent tupes.
     *
     * @return This class instance.
     * @throws IllegalArgumentException if either type or label have already been registered on this type adapter.
     */
    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type, String label) {
        if (type == null || label == null) {
            throw new NullPointerException();
        }
        if (subtypeToLabel.containsKey(type) || labelToSubtype.containsKey(label)) {
            throw new IllegalArgumentException("types and labels must be unique");
        }
        labelToSubtype.put(label, type);
        subtypeToLabel.put(type, label);
        return this;
    }

    /**
     * Registers type identified by its {@link Class#getSimpleName}. Labels are case sensitive.
     *
     * @param type The type of the child being registered.
     *
     * @return This class instance.
     * @throws IllegalArgumentException if either {@code type} or its simple name
     *     have already been registered on this type adapter.
     */
    public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> type) {
        return registerSubtype(type, type.getSimpleName());
    }

    /**
     * Creates the GSON Type Adapter
     * @param gson The instance of the GSON object.
     * @param type The type token to interpret children classes with.
     * @param <R> The parent class type.
     *
     * @return A new type adapter.
     */
    public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        if (type.getRawType() != baseType) {
            return null;
        }

        final Map<String, TypeAdapter<?>> labelToDelegate
                = new LinkedHashMap<>();
        final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate
                = new LinkedHashMap<>();
        for (Map.Entry<String, Class<?>> entry : labelToSubtype.entrySet()) {
            TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
            labelToDelegate.put(entry.getKey(), delegate);
            subtypeToDelegate.put(entry.getValue(), delegate);
        }

        return new TypeAdapter<R>() {
            @Override
            public R read(JsonReader in) {
                JsonElement jsonElement = Streams.parse(in);
                JsonElement labelJsonElement = jsonElement.getAsJsonObject().remove(typeFieldName);
                if (labelJsonElement == null) {
                    throw new JsonParseException("cannot deserialize " + baseType
                            + " because it does not define a field named " + typeFieldName);
                }
                String label = labelJsonElement.getAsString();
                @SuppressWarnings("unchecked") // registration requires that subtype extends T
                TypeAdapter<R> delegate = (TypeAdapter<R>) labelToDelegate.get(label);
                if (delegate == null) {
                    throw new JsonParseException("cannot deserialize " + baseType + " subtype named "
                            + label + "; did you forget to register a subtype?");
                }
                return delegate.fromJsonTree(jsonElement);
            }

            @Override
            public void write(JsonWriter out, R value) throws IOException {
                Class<?> srcType = value.getClass();
                @SuppressWarnings("unchecked") // registration requires that subtype extends T
                TypeAdapter<R> delegate = (TypeAdapter<R>) subtypeToDelegate.get(srcType);
                if (delegate == null) {
                    throw new JsonParseException("cannot serialize " + srcType.getName()
                            + "; did you forget to register a subtype?");
                }
                JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();
                JsonObject clone = new JsonObject();
                for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                    clone.add(e.getKey(), e.getValue());
                }
                Streams.write(clone, out);
            }
        }.nullSafe();
    }
}