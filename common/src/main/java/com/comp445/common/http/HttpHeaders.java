package com.comp445.common.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpHeaders extends HashMap<String, String> {

    public static HttpHeaders fromMap(Map<String, String> map) {
        HttpHeaders newMap = new HttpHeaders();
        newMap.putAll(map);
        return newMap;
    }

    public static HttpHeaders fromLines(List<String> lines) {
        return fromMap(
                lines.stream()
                    .map(String::trim)
                    .map(headerArg -> headerArg.split(":", 2))
                    .collect(Collectors.toMap(
                            headerArgSplit -> headerArgSplit[0].trim(),
                            headerArgSplit -> headerArgSplit[1].trim(),
                            (val1, val2) -> val1))
        );
    }

    public String toString() {
        return String.join("\n", this.toStringList());
    }

    public List<String> toStringList() {
        return this.keySet().stream()
            .map(key -> String.format("%s: %s", key, this.get(key)))
            .collect(Collectors.toList());
    }

}
