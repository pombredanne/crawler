package com.github.vmorev.crawler.beans;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.github.vmorev.crawler.utils.JsonHelper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.*;

/**
 * User: Valentin_Morev
 * Date: 15.02.13
 */
public abstract class SDBItem {

    public List<ReplaceableAttribute> toSDB() throws IOException {
        Map<String, Object> entityMap = JsonHelper.parseJson(JsonHelper.parseObject(this), new TypeReference<Map<String, Object>>() {
        });
        List<ReplaceableAttribute> attributes = new ArrayList<>();

        for (String key : entityMap.keySet())
            attributes.addAll(getAttributesFromMap(key, entityMap.get(key)));
        return attributes;
    }

    private List<ReplaceableAttribute> getAttributesFromMap(String key, Object entity) {
        List<ReplaceableAttribute> attributes = new ArrayList<>();
        if (entity instanceof ArrayList) {
            List props = (ArrayList) entity;
            attributes.addAll(getAttributesFromMap(key, props));
        } else if (entity instanceof LinkedHashMap) {
            Map<String, Object> entityMap = (LinkedHashMap) entity;
            for (String key1 : entityMap.keySet())
                attributes.addAll(getAttributesFromMap(key1, entityMap.get(key)));
        } else
            attributes.add(new ReplaceableAttribute(key, entity.toString(), true));
        return attributes;
    }


    public void fromSDB(List<Attribute> attributes) throws IOException {
        Map<String, Object> entityMap = new HashMap<>();
        for (Attribute attribute : attributes) {
            try {
                Long value = Long.valueOf(attribute.getValue());
                entityMap.put(attribute.getName(), value);
                continue;
            } catch (NumberFormatException e) {
                //just continue
            }
            try {
                Double value = Double.valueOf(attribute.getValue());
                entityMap.put(attribute.getName(), value);
                continue;
            } catch (NumberFormatException e) {
                //just continue
            }
            try {
                Boolean value = Boolean.parseBoolean(attribute.getValue());
                entityMap.put(attribute.getName(), value);
                continue;
            } catch (NumberFormatException e) {
                //just continue
            }
            try {
                Boolean value = Boolean.parseBoolean(attribute.getValue());
                entityMap.put(attribute.getName(), value);
                continue;
            } catch (NumberFormatException e) {
                //just continue
            }
/*
            try {
                LinkedHashMap value = (LinkedHashMap) attribute.getValue();
                entityMap.put(attribute.getName(), value);
                continue;
            } catch (NumberFormatException e) {
                //just continue
            }
*/
            entityMap.put(attribute.getName(), attribute.getValue());
        }
        JsonHelper.parseJson(JsonHelper.parseObject(entityMap), this.getClass());
    }
}
