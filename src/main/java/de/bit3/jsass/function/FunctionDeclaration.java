package de.bit3.jsass.function;

import java.lang.reflect.Method;
import java.util.List;

public class FunctionDeclaration {
    public static final String DEFAULT_VALUE_INDICATOR = "JAVA_RUNTIME_DEFAULT_VALUE";

    protected final String     signature;
    protected final Object     object;
    protected final Method     method;
    protected final Class<?>[] types;

    public FunctionDeclaration(String signature, Object object, Method method, Class<?>[] types) {
        this.signature = signature;
        this.object = object;
        this.method = method;
        this.types = types;
    }

    public String getSignature() {
        return signature;
    }

    public Object getObject() {
        return object;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?>[] getTypes() {
        return types;
    }

    public Object invoke(List<?> arguments) {
        Object[] args = new Object[types.length];

        for (int index = 0; index < args.length; index++) {
            Class<?> targetType = types[index];
            Object value;

            if (index >= arguments.size()) {
                value = null;
            } else {
                value = arguments.get(index);
            }

            if (null != value && !targetType.isAssignableFrom(value.getClass())) {
                if (String.class.isAssignableFrom(targetType)) {
                    value = value.toString();
                } else if (Byte.class.isAssignableFrom(targetType) || byte.class.isAssignableFrom(targetType)) {
                    if (value instanceof Number) {
                        value = ((Number) value).byteValue();
                    } else {
                        value = Byte.parseByte(value.toString());
                    }
                } else if (Short.class.isAssignableFrom(targetType) || short.class.isAssignableFrom(targetType)) {
                    if (value instanceof Number) {
                        value = ((Number) value).shortValue();
                    } else {
                        value = Short.parseShort(value.toString());
                    }
                } else if (Integer.class.isAssignableFrom(targetType) || int.class.isAssignableFrom(targetType)) {
                    if (value instanceof Number) {
                        value = ((Number) value).intValue();
                    } else {
                        value = Integer.parseInt(value.toString());
                    }
                } else if (Long.class.isAssignableFrom(targetType) || long.class.isAssignableFrom(targetType)) {
                    if (value instanceof Number) {
                        value = ((Number) value).longValue();
                    } else {
                        value = Long.parseLong(value.toString());
                    }
                } else if (Float.class.isAssignableFrom(targetType) || float.class.isAssignableFrom(targetType)) {
                    if (value instanceof Number) {
                        value = ((Number) value).floatValue();
                    } else {
                        value = Float.parseFloat(value.toString());
                    }
                } else if (Double.class.isAssignableFrom(targetType) || double.class.isAssignableFrom(targetType)) {
                    if (value instanceof Number) {
                        value = ((Number) value).doubleValue();
                    } else {
                        value = Double.parseDouble(value.toString());
                    }
                } else if (Boolean.class.isAssignableFrom(targetType) || boolean.class.isAssignableFrom(targetType)) {
                    value = Boolean.valueOf(value.toString());
                } else if (Character.class.isAssignableFrom(targetType) || char.class.isAssignableFrom(targetType)) {
                    value = value.toString().charAt(0);
                } else {
                    throw new RuntimeException(
                            String.format(
                                    "Cannot convert SASS type %s to Java type %s",
                                    value.getClass().getName(),
                                    targetType.getName()
                            )
                    );
                }
            }

            args[index] = value;
        }

        try {
            return method.invoke(object, args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}

