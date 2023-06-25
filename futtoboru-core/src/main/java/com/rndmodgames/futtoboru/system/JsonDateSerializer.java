package com.rndmodgames.futtoboru.system;

import java.time.LocalDateTime;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class JsonDateSerializer implements Json.Serializer<LocalDateTime> {

    @Override
    public void write (Json json, LocalDateTime date, Class knownType) {
        
        System.out.println("json: " + json + ", date: " + date + " knownType: " + knownType);
         
        json.writeObjectStart();
        json.writeValue("date", date.toString());
        json.writeObjectEnd();
     }

     @Override
     public LocalDateTime read (Json json, JsonValue jsonData, Class type) {
         
         System.out.println("json: " + json + ", jsonData: " + jsonData + "type: " + type);
         
         LocalDateTime date = LocalDateTime.parse(jsonData.toString());
         return date;
     }
}