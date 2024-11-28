package com.renodesor.taam.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.renodesor.taam.entity.BasicEntity;
import com.renodesor.taam.entity.TaamUser;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class Utils {

    private Utils() {}
    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public static Map convertObjectToMap(Object object) throws IllegalAccessException {
        Map<String, Object> objectInMap = new HashMap();
        List<Field> fields = getAllFields(object.getClass());

        for (Field field : fields) {
            field.setAccessible(true); // Make private fields accessible
            objectInMap.put(field.getName(), field.get(object)); // Get field name and value
        }
        return objectInMap;
    }

    public static <T> T convertMapToObject(Map<String, Object> map, Class<T> objectType) throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        T obj = objectType.newInstance(); // Create a new instance of the class
        // Iterate over the map and set the corresponding fields in the object
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();

            // Get the field of the class based on the field name
            setField(fieldName, fieldValue, obj, objectType);

            // Set the field's value to the object

        }
        return obj;
    }

    private static void setField(String fieldName, Object fieldValue, Object obj, Class<?> objectType) {
        if(fieldValue != null) {
            try {
                Field field = objectType.getDeclaredField(fieldName);
                field.setAccessible(true); // Make private fields accessible
                field.set(obj, fieldValue);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                if (objectType.getSuperclass() != null) {
                    setField(fieldName, fieldValue, obj, objectType.getSuperclass());
                }
            }
        }
    }

    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();

        // Get fields from the current class
        for (Field field : clazz.getDeclaredFields()) {
            fields.add(field);
        }

        // Recursively get fields from the superclass
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            fields.addAll(getAllFields(superclass));
        }

        return fields;
    }

    public static <T> T mergeTwoObjects(Object objectA, Object objectB, Class<T> objectType) throws IllegalAccessException, NoSuchFieldException, InstantiationException {
        Map objectAInMap = convertObjectToMap(objectA);
        Map objectBInMap = convertObjectToMap(objectB);
        objectBInMap.forEach((key, value) -> {
            if (value != null) {
                objectAInMap.put(key, value);
            }});

        return (T) convertMapToObject(objectAInMap, objectType);
    }

    public static String getCurrentUsername(TaamUser taamUser) {
        //return String.format("%s %s",taamUser.getFirstName(), taamUser.getLastName());
        return taamUser.getEmail();
    }

    public static void setAuditInfo(BasicEntity basicEntity, String operationType, TaamUser taamUser) {
        if("CREATE".equals(operationType)) {
            if(basicEntity.getId() ==  null) {
                basicEntity.setId(UUID.randomUUID());
            }
            if(basicEntity.getCreatedBy() == null && taamUser != null) {
                basicEntity.setCreatedBy(Utils.getCurrentUsername(taamUser));
            }
            if(basicEntity.getCreatedOn() == null) {
                basicEntity.setCreatedOn(LocalDateTime.now());
            }
        } else {
            if(basicEntity.getUpdatedBy() == null && taamUser != null) {
                basicEntity.setUpdatedBy(Utils.getCurrentUsername(taamUser));
            }
            if(basicEntity.getUpdatedOn() == null) {
                basicEntity.setUpdatedOn(LocalDateTime.now());
            }
        }
        if(taamUser == null) {
            log.error("Unable to get user info for setting auditing info (taamUser is null)");
        }
    }

    public  static <T> T convertJsonToObject(String json, Class<T> objectType) {
        return gson.fromJson(json, objectType);
    }

    public static String convertObjectToJson(Object object) {
        return gson.toJson(object);
    }
}
