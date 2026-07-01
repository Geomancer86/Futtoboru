package com.rndmodgames.futtoboru.system.serializers;

import java.time.LocalDate;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class JsonLocalDateSerializer implements Json.Serializer<LocalDate> {

    @Override
    public void write(Json json, LocalDate date, Class knownType) {
        json.writeObjectStart();
        json.writeType(LocalDate.class);
        json.writeValue("value", date.toString());
        json.writeObjectEnd();
    }

    @Override
    public LocalDate read(Json json, JsonValue jsonData, Class type) {
        return LocalDate.parse(jsonData.getString("value"));
    }
}
