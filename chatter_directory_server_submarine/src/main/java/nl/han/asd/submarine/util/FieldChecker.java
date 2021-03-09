package nl.han.asd.submarine.util;

import com.google.common.base.CaseFormat;
import nl.han.asd.submarine.exceptions.NoSuchMethodRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class FieldChecker {

    private FieldChecker() {
    }

    private static <T> boolean checkFieldNotBlankOrNull(String field, T object) {
        try {
            var result = object.getClass().getMethod("get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, field)).invoke(object);
            return result != null && ((!(result instanceof String)) || !((String) result).isBlank());

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new NoSuchMethodRuntimeException("Something went wrong. A getter method was not found. Contact the system admin", e.getCause());
        }
    }

    public static <T> boolean areFieldsNotBlankOrNull(String[] fields, T object) {
        return Arrays.stream(fields).allMatch(field -> FieldChecker.checkFieldNotBlankOrNull(field, object));
    }
}
