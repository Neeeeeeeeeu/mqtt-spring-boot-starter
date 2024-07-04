package com.github.neu.mqtt.core;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface JacksonConverter {

    ObjectMapper createObjectMapper();
}
