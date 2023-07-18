package com.rndmodgames.futtoboru.system.serializers;

import java.time.LocalDateTime;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class JsonDateSerializer implements Json.Serializer<LocalDateTime> {

    @Override
    public void write (Json json, LocalDateTime date, Class knownType) {
        
//        System.out.println("json: " + json + ", date: " + date + " knownType: " + knownType);
         
        json.writeObjectStart();
        json.writeType(java.time.LocalDateTime.class);
        json.writeValue("value", date.toString());
        json.writeObjectEnd();
     }

     @Override
     public LocalDateTime read (Json json, JsonValue jsonData, Class type) {
         
//         System.out.println("json: " + json + ", jsonData: " + jsonData + "type: " + type);
         
         LocalDateTime date = LocalDateTime.parse(jsonData.getString("value"));
         return date;
     }
}