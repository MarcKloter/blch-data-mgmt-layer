package bdml.core.helper;

import java.util.Collection;
import java.util.Objects;

public class Assert {
    /**
     * Wraps Objects.requireNonNull for uniform messages.
     *
     * @param obj object reference to check
     * @param param parameter name
     */
    public static void requireNonNull(Object obj, String param) {
        Objects.requireNonNull(obj, String.format("Parameter '%s' cannot be null.", param));
    }

    /**
     * Checks that the specified Collection is not empty and throws a customized IllegalArgumentException if it is.
     * In case the Collection is null, a NullPointerException is thrown.
     *
     * @param coll the Collection to check
     * @param param parameter name
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
     * @param string the String to check
     * @param param parameter name
     */
    public static void requireNonEmpty(String string, String param) {
        requireNonNull(string, param);
        if(string.isEmpty())
            throw new IllegalArgumentException(String.format("No '%s' provided.", param));
    }
}
