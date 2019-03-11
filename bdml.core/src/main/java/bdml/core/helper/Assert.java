package bdml.core.helper;

import java.util.Collection;
import java.util.Objects;

public class Assert {
    private Assert() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Wraps Objects.requireNonNull for uniform messages.
     *
     * @param obj object reference to check for nullity
     * @param param parameter name
     * @throws NullPointerException if {@code obj} is {@code null}.
     */
    public static void requireNonNull(Object obj, String param) {
        Objects.requireNonNull(obj, String.format("Parameter '%s' cannot be null.", param));
    }

    /**
     * Checks that the specified Collection is not empty and throws a customized IllegalArgumentException if it is.
     *
     * @param coll the collection to check
     * @param param parameter name
     * @throws NullPointerException if {@code coll} is {@code null}.
     */
    public static void requireNonEmpty(Collection coll, String param) {
        requireNonNull(coll, param);
        if(coll.isEmpty())
            throw new IllegalArgumentException(String.format("No '%s' provided.", param));
    }

    /**
     * Checks that the specified String is not empty and throws a customized IllegalArgumentException if it is.
     * In case the String is null, a NullPointerException is thrown.
     *
     * @param string the string to check
     * @param param parameter name
     * @throws IllegalArgumentException if {@code string} is empty
     */
    public static void requireNonEmpty(String string, String param) {
        requireNonNull(string, param);
        if(string.isEmpty())
            throw new IllegalArgumentException(String.format("No '%s' provided.", param));
    }
}
