package com.hs1n.lifeXp_challenge.config;

/**
 * Хранит конфигурацию одного атрибута для кусочно-линейной интерполяции.
 * startValue — модификатор на уровне опыта 0.
 * midValue   — модификатор на уровне опыта maxLevel / 2.
 * endValue   — модификатор на уровне опыта maxLevel.
 */
public class AttributeConfigNode {
    private double startValue;
    private double midValue;
    private double endValue;
    private final DifficultyDirection direction;

    private FormulaMode formulaMode = FormulaMode.INTERPOLATION;
    private double formulaBase = 0.0;
    private double formulaMultiplier = 1.0;
    private double formulaExponent = 1.0;

    public AttributeConfigNode(double startValue, double midValue, double endValue, DifficultyDirection direction) {
        this.startValue = startValue;
        this.midValue = midValue;
        this.endValue = endValue;
        this.direction = direction;
    }

    public double getStartValue() { return startValue; }
    public void setStartValue(double startValue) { this.startValue = startValue; }

    public double getMidValue() { return midValue; }
    public void setMidValue(double midValue) { this.midValue = midValue; }

    public double getEndValue() { return endValue; }
    public void setEndValue(double endValue) { this.endValue = endValue; }

    public DifficultyDirection getDirection() { return direction; }

    public FormulaMode getFormulaMode() { return formulaMode; }
    public void setFormulaMode(FormulaMode formulaMode) { this.formulaMode = formulaMode; }

    public double getFormulaBase() { return formulaBase; }
    public void setFormulaBase(double formulaBase) { this.formulaBase = formulaBase; }

    public double getFormulaMultiplier() { return formulaMultiplier; }
    public void setFormulaMultiplier(double formulaMultiplier) { this.formulaMultiplier = formulaMultiplier; }

    public double getFormulaExponent() { return formulaExponent; }
    public void setFormulaExponent(double formulaExponent) { this.formulaExponent = formulaExponent; }

    public AttributeConfigNode copy() {
        AttributeConfigNode node = new AttributeConfigNode(startValue, midValue, endValue, direction);
        node.setFormulaMode(this.formulaMode);
        node.setFormulaBase(this.formulaBase);
        node.setFormulaMultiplier(this.formulaMultiplier);
        node.setFormulaExponent(this.formulaExponent);
        return node;
    }
}
