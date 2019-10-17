package edu.ku.brc.specify.ui;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

public class CatalogNumberStringRegExUIFieldFormatter extends CatalogNumberStringUIFieldFormatter implements UIFieldFormatterIFace {
    private String regEx;

    @Override
    public boolean isValid(String value) {
        return super.isValid(value);
    }

    public String getRegEx() {
        return regEx;
    }

    public void setRegEx(String regEx) {
        this.regEx = regEx;
    }
}
