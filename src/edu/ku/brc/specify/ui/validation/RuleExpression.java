/* Filename:    $RCSfile: FormValidator.java,v $
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

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.jexl.JexlContext;

/**
 * 
 * @author rods
 *
 */
public class RuleExpression
{
    private static Log log = LogFactory.getLog(RuleExpression.class);
    
    protected String     name;
    protected String     rule;
    protected Expression expression;
    
    /**
     * Constructor
     * @param name the name of the rule 
     * @param rule the rule 
     */
    public RuleExpression(final String name, 
                          final String rule)
    {
        this.name = name;
        this.rule = rule;
        
        try
        {
            expression = ExpressionFactory.createExpression( rule );
        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * Evaluates the rule within the context
     * @param context the context to evaluate the expression
     * @return returns the return Object of the evaluation
     * @throws Exception
     */
    public Object evaluate(final JexlContext context) throws Exception
    {
        Object result = expression.evaluate(context);
        log.info("Result "+result+" for "+name+"  "+rule);
        return result;
    }
    
    /**
     * @return Returns the exp.
     */
    public Expression getExpressionX()
    {
        return expression;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

}
