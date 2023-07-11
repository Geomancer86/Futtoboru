package com.rndmodgames.futtoboru.system.serializers;

import java.math.BigDecimal;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class BigDecimalSerializer implements Json.Serializer<BigDecimal> {

    @Override
    public void write (Json json, BigDecimal number, Class knownType) {
        
//        System.out.println("json: " + json + ", number: " + number + " knownType: " + knownType);
         
        json.writeObjectStart();
        json.writeType(BigDecimal.class);
        json.writeValue("value", number.toString());
        json.writeObjectEnd();
     }

     @Override
     public BigDecimal read (Json json, JsonValue jsonData, Class type) {
         
//         System.out.println("json: " + json + ", jsonData: " + jsonData + "type: " + type);
         
         BigDecimal number = new BigDecimal(jsonData.getString("value"));
         return number;
     }
}