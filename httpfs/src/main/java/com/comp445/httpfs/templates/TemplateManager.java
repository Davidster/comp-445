package com.comp445.httpfs.templates;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class TemplateManager {

    public static String TEMPLATE_400 = "400.html";
    public static String TEMPLATE_403 = "403.html";
    public static String TEMPLATE_404 = "404.html";
    public static String TEMPLATE_500 = "500.html";
    public static String TEMPLATE_DIRECTORY_LISTING = "directoryListing.html";

    public static void init() {
        TEMPLATE_400 = getTemplate(TEMPLATE_400);
        TEMPLATE_403 = getTemplate(TEMPLATE_403);
        TEMPLATE_404 = getTemplate(TEMPLATE_404);
        TEMPLATE_500 = getTemplate(TEMPLATE_500);
        TEMPLATE_DIRECTORY_LISTING = getTemplate(TEMPLATE_DIRECTORY_LISTING);
    }

    public static String getTemplate(String template) {
        return new BufferedReader(new InputStreamReader(
                TemplateManager.class.getClassLoader().getResourceAsStream(template)))
                    .lines().collect(Collectors.joining(""));
    }

}
