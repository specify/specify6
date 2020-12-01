package edu.ku.brc.specify.ui;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

public class CatalogNumberStringRegExUIFieldFormatter extends CatalogNumberStringUIFieldFormatter implements UIFieldFormatterIFace {
    private String regEx = "";

    public CatalogNumberStringRegExUIFieldFormatter() {
        super();
    }


    @Override
    public boolean isValid(String value) {
        boolean regexMatch = java.util.regex.Pattern.matches(getRegEx(), value);
        int len = getLength();
        System.out.println("CatalogNumberStringRegExUIFieldFormatter.isValid(value): value = " + value + ", " + regexMatch + ", " + len)    ;
        return java.util.regex.Pattern.matches(getRegEx(), value);
    }

    public String getRegEx() {
        return regEx;
    }

    public void setRegEx(String regEx) {
        this.regEx = regEx;
    }
}
