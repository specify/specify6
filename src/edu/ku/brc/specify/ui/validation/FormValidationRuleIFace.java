/* Filename:    $RCSfile: FormValidationRuleIFace.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/16 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ku.brc.specify.ui.validation;

import org.apache.commons.jexl.JexlContext;

/**
 * This interface enables a Form validator to have many different types of rules for performing 
 * any time of validation within the form. Most rules are used to 
 * 
 * @author rods
 *
 */
public interface FormValidationRuleIFace
{

    public enum Scope {Field, Form};
    
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
