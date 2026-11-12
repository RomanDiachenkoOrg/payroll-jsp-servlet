package com.acme.payroll.service;

import java.util.LinkedHashMap;
import java.util.Map;

/** Field name to error message, in the order the errors were found. */
public class ValidationErrors {

    private final Map<String, String> errors = new LinkedHashMap<String, String>();

    public void add(String field, String message) {
        if (!errors.containsKey(field)) {
            errors.put(field, message);
        }
    }

    public boolean has(String field) {
        return errors.containsKey(field);
    }

    public boolean isEmpty() {
        return errors.isEmpty();
    }

    public Map<String, String> getMap() {
        return errors;
    }

    public int size() {
        return errors.size();
    }
}
