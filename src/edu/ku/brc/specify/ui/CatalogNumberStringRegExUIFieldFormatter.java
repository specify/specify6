package edu.ku.brc.specify.ui;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

public class CatalogNumberStringRegExUIFieldFormatter extends CatalogNumberStringUIFieldFormatter implements UIFieldFormatterIFace {
    private String regEx = "";

    public CatalogNumberStringRegExUIFieldFormatter() {
        super();
    }


    @Override
    public boolean isValid(String value) {
        return java.util.regex.Pattern.matches(getRegEx(), value) && value.length() <= getLength();
    }

    public String getRegEx() {
        return regEx;
    }

    public void setRegEx(String regEx) {
        this.regEx = regEx;
    }
}
