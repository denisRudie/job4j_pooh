package ru.job4j;

public class Item {
    private String name;

    public Item(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Item copyOf() {
        return new Item(this.getName());
    }
}
