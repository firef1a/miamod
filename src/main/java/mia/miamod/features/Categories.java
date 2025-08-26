package mia.miamod.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public enum Categories {
    GENERAL(new Category("general")),
    DEV(new Category("development")),
    SUPPORT(new Category("support")),
    MODERATION(new Category("moderation")),
    INTERNAL(new Category("internal"));

    private final Category category;

    Categories(Category category) {
        this.category = category;
    }

    public static ArrayList<Category> getCategories() { return Arrays.stream(values()).map(Categories::getCategory).collect(Collectors.toCollection(ArrayList::new)); }
    public Category getCategory() { return this.category; }
}
