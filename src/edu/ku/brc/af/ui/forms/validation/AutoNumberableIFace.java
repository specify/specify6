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
