package com.hs1n.lifeXp_challenge.config;

/**
 * Режим расчёта прогрессии атрибута.
 *
 * INTERPOLATION — классическая кусочно-линейная интерполяция
 *                 по трём точкам (start / mid / end).
 * FORMULA       — кастомная формула:
 *                 Result = base + (level × multiplier) ^ exponent
 */
public enum FormulaMode {
    INTERPOLATION("Interpolation (3-point)"),
    FORMULA("Custom Formula");

    private final String label;

    FormulaMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
