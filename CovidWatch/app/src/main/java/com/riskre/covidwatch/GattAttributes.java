package com.riskre.covidwatch;

import java.util.HashMap;

/**
 * A collection of UUIDs to name mappings for CovidWatch
 */
public class GattAttributes {

    private static HashMap<String, String> attributes = new HashMap();

    static {
        // Services
        attributes.put("C019FB3E-B1FC-45E9-9274-1EA259249C80", "HumanPresenceService");

        // Characteristics.
        attributes.put("C019FB3E-B1FC-45E9-9274-1EA259249C81", "PotentiallySick");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
