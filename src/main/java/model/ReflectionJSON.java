package model;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class ReflectionJSON {

    public static byte intToByte(int conv) {
        return (byte) conv;
    }

    public static short intToShort(int conv) {
        return (short) conv;
    }

    public static char intToChar(int conv) {
        return (char) conv;
    }

    public static int byteToInt(byte conv) {
        return conv;
    }

    public static int shortToInt(short conv) {
        return conv;
    }

    public static int charToInt(char conv) {
        return conv;
    }

    /**
     * @return Collection of unextracted optional field names
     */
    public static Set<String> extract(JSONObject source, Object dest, String errorContext) {
        Collection<Field> fields = getAllFields(dest.getClass());
        Set<String> unextracted = new HashSet<>();
        for(Field field : fields) {
            boolean optional = field.getAnnotation(Direct.Optional.class) != null;
            if(field.getAnnotation(Direct.class) != null || optional) {
                try {
                    if(!optional || source.has(field.getName())) {
                        field.setAccessible(true);
                        if(field.getType().isPrimitive()) {
                            // extract primitive
                            if(field.getType().equals(Integer.TYPE)) {
                                field.setInt(dest, source.getInt(field.getName()));
                            } else if(field.getType().equals(Boolean.TYPE)) {
                                field.setBoolean(dest, source.getBoolean(field.getName()));
                            } else if(field.getType().equals(Float.TYPE)) {
                                field.setFloat(dest, source.getFloat(field.getName()));
                            } else if(field.getType().equals(Double.TYPE)) {
                                field.setDouble(dest, source.getDouble(field.getName()));
                            } else if(field.getType().equals(Long.TYPE)) {
                                field.setLong(dest, source.getLong(field.getName()));
                            } else if(field.getType().equals(Byte.TYPE)) {
                                field.setByte(dest, intToByte(source.getInt(field.getName())));
                            } else if(field.getType().equals(Short.TYPE)) {
                                field.setShort(dest, intToShort(source.getInt(field.getName())));
                            } else if(field.getType().equals(Character.TYPE)) {
                                field.setInt(dest, intToChar(source.getInt(field.getName())));
                            } else {
                                throw new Error("Field (" + field.getName() + ") marked @Direct was of unsupported primitive type: " + field.getType());
                            }
                        } else {
                            if(field.getType().equals(String.class)) {
                                field.set(dest, source.getString(field.getName()));
                            } else if(field.getType().equals(BigDecimal.class)) {
                                field.set(dest, source.getBigDecimal(field.getName()));
                            } else if(field.getType().equals(BigInteger.class)) {
                                field.set(dest, source.getBigInteger(field.getName()));
                            } else if(field.getType().equals(Number.class)) {
                                field.set(dest, source.getNumber(field.getName()));
                            } else {
                                field.set(dest, CustomConversionJSON.createFieldFromJSON(field.getType(), field.getName(), source));
                            }
                        }
                    } else {
                        unextracted.add(field.getName());
                    }
                } catch(IllegalAccessException e) {
                    throw new Error("Field (" + field.getName() + ") marked @Direct was inaccessible for extraction");
                } catch(JSONException e) {
                    throw new JSONException(e.getMessage() + " " + errorContext, e.getCause());
                }
            }
        }
        return unextracted;
    }

    public static JSONObject makeJSON(Object source) {
        JSONObject dest = new JSONObject();
        Collection<Field> fields = getAllFields(source.getClass());
        for(Field field : fields) {
            // TODO: use default values of optionals to selectively add to the JSON file
            if(field.getAnnotation(Direct.class) != null || field.getAnnotation(Direct.Optional.class) != null) {
                field.setAccessible(true);
                try {
                    if(field.getType().isPrimitive()) {
                        // extract primitive
                        if(field.getType().equals(Integer.TYPE)) {
                            dest.put(field.getName(), field.getInt(source));
                        } else if(field.getType().equals(Boolean.TYPE)) {
                            dest.put(field.getName(), field.getBoolean(source));
                        } else if(field.getType().equals(Float.TYPE)) {
                            dest.put(field.getName(), field.getFloat(source));
                        } else if(field.getType().equals(Double.TYPE)) {
                            dest.put(field.getName(), field.getDouble(source));
                        } else if(field.getType().equals(Long.TYPE)) {
                            dest.put(field.getName(), field.getLong(source));
                        } else if(field.getType().equals(Byte.TYPE)) {
                            dest.put(field.getName(), byteToInt(field.getByte(source)));
                        } else if(field.getType().equals(Short.TYPE)) {
                            dest.put(field.getName(), shortToInt(field.getShort(source)));
                        } else if(field.getType().equals(Character.TYPE)) {
                            dest.put(field.getName(), charToInt(field.getChar(source)));
                        } else {
                            throw new Error("Field (" + field.getName() + ") marked @Direct was of unsupported primitive type: " + field.getType());
                        }
                    } else {
                        if(field.getType().equals(String.class) ||
                                field.getType().equals(BigDecimal.class) ||
                                field.getType().equals(BigInteger.class) ||
                                field.getType().equals(Number.class)) {
                            // pray this works
                            dest.put(field.getName(), field.get(source));
                        } else {
                            CustomConversionJSON.putFieldIntoJSON(dest, field.getName(), field.getType(), field.get(source));
                        }
                    }
                } catch(IllegalAccessException e) {
                    throw new Error("Field (" + field.getName() + ") marked @Direct was inaccessible for extraction");
                }
            }
        }
        return dest;
    }

    private static Collection<Field> getAllFields(Class<?> c) {
        ArrayList<Field> fields = new ArrayList<>();
        do {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        } while((c = c.getSuperclass()) != null);
        return fields;
    }
}
