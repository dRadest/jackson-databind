package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

@JacksonStdImpl
public final class StringDeserializer extends StdScalarDeserializer<String>
{
    private static final long serialVersionUID = 1L;

    /**
     * @since 2.2
     */
    public final static StringDeserializer instance = new StringDeserializer();
    
    public StringDeserializer() { super(String.class); }

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        // Issue#381
        if (jp.getCurrentToken() == JsonToken.START_ARRAY && ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
            jp.nextToken();
            final String parsed = _parseString(jp, ctxt);
            if (jp.nextToken() != JsonToken.END_ARRAY) {
                throw ctxt.wrongTokenException(jp, JsonToken.END_ARRAY, 
                        "Attempted to unwrap single value array for single 'String' value but there was more than a single value in the array");
            }            
            return parsed;            
        }
        // 22-Sep-2012, tatu: For 2.1, use this new method, may force coercion:
        String text = jp.getValueAsString();
        if (text != null) {
            return text;
        }
        // [JACKSON-330]: need to gracefully handle byte[] data, as base64
        JsonToken curr = jp.getCurrentToken();
        if (curr == JsonToken.VALUE_EMBEDDED_OBJECT) {
            Object ob = jp.getEmbeddedObject();
            if (ob == null) {
                return null;
            }
            if (ob instanceof byte[]) {
                return Base64Variants.getDefaultVariant().encode((byte[]) ob, false);
            }
            // otherwise, try conversion using toString()...
            return ob.toString();
        }
        throw ctxt.mappingException(_valueClass, curr);
    }

    // 1.6: since we can never have type info ("natural type"; String, Boolean, Integer, Double):
    // (is it an error to even call this version?)
    @Override
    public String deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        return deserialize(jp, ctxt);
    }
}
