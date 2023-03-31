/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.log4j.Logger;

/**
 * This class is an implementation of a JEXL expression. The rule has a name and the Java expression
 * and it is passed in a context to which it evaludates the rule. The conttext comes from the form so all the
 * ui controls are available by name in the context.
 
 * @code_status Beta
 **
 * @author rods
 *
 */
public class RuleExpression implements FormValidationRuleIFace
{
    private static final Logger log = Logger.getLogger(RuleExpression.class);

    protected String     id;
    protected String     rule;
    protected Expression expression;

    /**
     * Constructor
     * @param id the id of the rule
     * @param rule the rule
     */
    public RuleExpression(final String id,
                          final String rule)
    {
        this.id   = id;
        this.rule = rule;

        try
        {
            expression = ExpressionFactory.createExpression( rule );

        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RuleExpression.class, ex);
            log.error(ex);
            //ex.printStackTrace();
        }
    }

    /**
     * Cleanup internal data
     */
    public void cleanUp()
    {
        expression = null;
    }

    //-----------------------------------------------------------
    // FormValidationRuleIFace
    //-----------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.FormValidationRuleIFace#evaluate(org.apache.commons.jexl.JexlContext)
     */
    public boolean evaluate(final JexlContext context)
    {
        try
        {
            //log.debug("Rule "+rule+" for id "+id+"  "+id);
            Object result = expression.evaluate(context);
            //log.debug("Result "+result+" for "+id+"  "+rule);
            if (result instanceof Boolean)
            {
                return (Boolean)result;
            }
            // else
            log.debug("the return from the evaluation is of class "+result);
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RuleExpression.class, ex);
            log.error(ex);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.FormValidationRuleIFace#getScope()
     */
    public Scope getScope()
    {
        return FormValidationRuleIFace.Scope.Field;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.FormValidationRuleIFace#getId()
     */
    public String getId()
    {
        return id;
    }


}
