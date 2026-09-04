package com.hs1n.lifeXp_challenge.config;

/**
 * Определяет направление сложности для атрибута.
 * DIRECT — увеличение значения упрощает игру (напр. HP, урон).
 * INVERSE — увеличение значения усложняет игру.
 */
public enum DifficultyDirection {
    DIRECT("Больше = Легче"),
    INVERSE("Больше = Сложнее");

    private final String label;

    DifficultyDirection(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
