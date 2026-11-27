package com.rndmodgames.futtoboru.system.serializers;

import java.math.BigDecimal;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class BigDecimalSerializer implements Json.Serializer<BigDecimal> {

    @Override
    public void write (Json json, BigDecimal number, Class knownType) {
        
         
        json.writeObjectStart();
        json.writeType(BigDecimal.class);
        json.writeValue("value", number.toString());
        json.writeObjectEnd();
     }

     @Override
     public BigDecimal read (Json json, JsonValue jsonData, Class type) {
         
         
         BigDecimal number = new BigDecimal(jsonData.getString("value"));
         return number;
     }
}