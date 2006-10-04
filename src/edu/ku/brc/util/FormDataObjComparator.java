/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.util;

import java.util.Comparator;

import edu.ku.brc.ui.forms.FormDataObjIFace;

/**
 * A comparator for sorting objects that implement the FormDataObjIFace interface.
 *
 * @code_status Complete
 * @author jstewart
 */
public class FormDataObjComparator implements Comparator<FormDataObjIFace>
{
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(FormDataObjIFace o1, FormDataObjIFace o2)
    {
        return o1.getIdentityTitle().compareTo(o2.getIdentityTitle());
    }
}
