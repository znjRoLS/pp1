package rosko.bojan.semanticcontext;

import rosko.bojan.ExpressionToken;


/**
 * Created by rols on 5/9/17.
 */
public class SemanticParameters{
    public ExpressionToken expression, expression2;
    public Integer value;
    public String type;
    public String name;

    public SemanticParameters(ExpressionToken expression, ExpressionToken expression2, Integer value, String type, String name) {
        this.expression = expression;
        this.expression2 = expression2;
        this.value = value;
        this.type = type;
        this.name = name;
    }

    public SemanticParameters() {
        this(null, null, null,null,null);
    }

    public SemanticParameters setExpression(ExpressionToken expression) {
        this.expression = expression;
        return this;
    }

    public SemanticParameters setExpression2(ExpressionToken expression2) {
        this.expression2 = expression2;
        return this;
    }

    public SemanticParameters setValue(Integer value) {
        this.value = value;
        return this;
    }

    public SemanticParameters setValue(SemanticContext.ErrorType value) {
        return setValue(value.ordinal());
    }

    public SemanticParameters setType(String type) {
        this.type = type;
        return this;
    }

    public SemanticParameters setName(String name) {
        this.name = name;
        return this;
    }

    public String toString() {
        return "expression: " + expression + " | " +
                "expression2: " + expression2 + " | " +
                "value: " + value + " | " +
                "type: " + type + " | " +
                "name: " + name;
    }
}
