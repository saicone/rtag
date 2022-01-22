package com.saicone.rtag;

import java.util.Map;

public interface RtagDeserializer<T> {

    String getOutID();

    T deserialize(Map<String, Object> compound);
}
