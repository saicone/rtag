package com.saicone.rtag;

import java.util.Map;

public interface RtagSerializer<T> {

    String getInID();

    Map<String, Object> serialize(T object);
}
