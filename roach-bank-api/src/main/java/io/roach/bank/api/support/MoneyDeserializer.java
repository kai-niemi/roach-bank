package io.roach.bank.api.support;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class MoneyDeserializer extends JsonDeserializer<Money> {
    @Override
    public Money deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        return Money.parse(jp.readValueAs(String.class));
    }
}
