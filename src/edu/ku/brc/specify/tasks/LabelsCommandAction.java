/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.tasks;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.ui.CommandAction;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 30, 2006
 *
 */
public class LabelsCommandAction extends CommandAction
{
    protected Taskable orginatingTask = null;
    protected String reportType;
    protected String reportSubType;
    
    public LabelsCommandAction(final String action, 
                               final String reportType, 
                               final String reportSubType, 
                               final Object data)
    {
        super(LabelsTask.LABELS, action, data);
        
        this.reportType    = reportType;
        this.reportSubType = reportSubType;
    }

    public LabelsCommandAction(final String type, 
                               final String action, 
                               final Object data, 
                               final Taskable origTask)
    {
        super(type, action, data);
        
        orginatingTask = origTask;
        orginatingTask = origTask;
        reportType     = null;
        reportSubType  = null;

    }

    public String getReportSubType()
    {
        return reportSubType;
    }

    public String getReportType()
    {
        return reportType;
    }

    public Taskable getOrginatingTask()
    {
        return orginatingTask;
    }
    
}
