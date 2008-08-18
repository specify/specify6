/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.af.ui.forms.validation;

/**
 * Interface for specifying whether a form control supports auto numbering, the auto-numerbering is done by the formatter.
 * So ultimately it is whether the current formatter supports auto-numbering. The point of asking the control, is really
 * whether it can contain formatters that may or may not support auto-numbering.
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * Dec 14, 2007
 *
 */
public interface AutoNumberableIFace
{
    /**
     * @return whether the formatter is an auto-numberer
     */
    public abstract boolean isFormatterAutoNumber();
    
    /**
     * Increments to the next number in the series.
     */
    public abstract void updateAutoNumbers();
    
    /**
     * Tells the control to turn on or off the auto-numbering.
     * @param turnOn true turns it on
     */
    public abstract void setAutoNumberEnabled(boolean turnOn);
    
}
