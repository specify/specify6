/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.af.ui.forms.validation;

import org.apache.commons.jexl.JexlContext;

/**
 * This interface enables a Form validator to have many different types of rules for performing 
 * any time of validation within the form. Most rules are used to 
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public interface FormValidationRuleIFace
{

    public enum Scope {Field, Form}
    
    /**
     * Evaluates the rule within the context
     * @param context the context to evaluate the expression
     * @return true if the java expression evaluated to true, otherwise false
     */
    public boolean evaluate(final JexlContext context);
    
    /**
     * Returns the scope of the rule. 
     * The scope indicates whether the validation rule is for a field or for the entire form.
     * @return the scope of the rule. 
     */
    public Scope getScope(); 
    
    /**
     * Returns the id of the rule
     * @return the id of the rule
     */
    public String getId();
    
}
