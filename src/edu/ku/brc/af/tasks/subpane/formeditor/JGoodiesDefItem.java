/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.tasks.subpane.formeditor;

import org.apache.commons.lang.StringUtils;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 27, 2007
 *
 */
public class JGoodiesDefItem
{
    private String[]     prefixes    = {"f", "fill", "l", "left", "r", "right", "c", "center", "t", "top", "b", "bottom"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$
    private ALIGN_TYPE[] prefixeVals = {ALIGN_TYPE.Fill,ALIGN_TYPE.Fill, ALIGN_TYPE.Left,ALIGN_TYPE.Left, ALIGN_TYPE.Right,ALIGN_TYPE.Right, ALIGN_TYPE.Center,ALIGN_TYPE.Center, ALIGN_TYPE.Top,ALIGN_TYPE.Top, ALIGN_TYPE.Bottom,ALIGN_TYPE.Bottom};
    
    public enum MINMAX_TYPE  {None, Min, Max}
    public enum SIZE_TYPE    {Pixels, Dlus}
    public enum ALIGN_TYPE   {None, Fill, Left, Center, Right, Top, Bottom}
       

    protected MINMAX_TYPE minMax  = MINMAX_TYPE.None;
    protected int         len     = -1;
    protected SIZE_TYPE   type    = SIZE_TYPE.Pixels;
    
    protected boolean     isGrow;
    protected ALIGN_TYPE  align   = ALIGN_TYPE.None;
    
    protected boolean     isPreferredSize = true;
    
    // For UI
    protected boolean     isInUse;
    
    public JGoodiesDefItem()
    {
        
    }
    
    public JGoodiesDefItem(final String defArg, final boolean isRow)
    {
        parse(defArg, isRow);
    }
    
    /**
     * @param defArg
     * @param isRow
     */
    public void parse(final String defArg, final boolean isRow)
    {
        String def = defArg;
        if (StringUtils.isEmpty(def))
        {
            def = "p"; //$NON-NLS-1$
        } else
        {
            def = defArg.toLowerCase();
        }
        
        if (def.startsWith("min")) //$NON-NLS-1$
        {
            minMax = MINMAX_TYPE.Min;
            parseMinMax(def);
            
        } else if (def.startsWith("max")) //$NON-NLS-1$
        {
            minMax = MINMAX_TYPE.Max;
            parseMinMax(def);
            
        } else 
        {
            String  adjustedDef = def;
            int inx = 0;
            for (String pfx : prefixes)
            {
                if (def.startsWith(pfx))
                {
                    adjustedDef = def.substring(pfx.length()+1, def.length());
                    align       = prefixeVals[inx];
                    break;
                }
                inx++;
            }
            
            if (StringUtils.isNumeric(adjustedDef.substring(0,1)))
            {
                parseNum(adjustedDef);
                isPreferredSize = false;
            } else
            {
                isPreferredSize = true;
            }
        }
        isGrow = def.endsWith(":g") || def.endsWith(":grow"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    protected void parseNum(final String def)
    {
        int inx = def.indexOf("px"); //$NON-NLS-1$
        if (inx > -1)
        {
            len = Integer.parseInt(def.substring(0, inx));
            type = SIZE_TYPE.Pixels;
        } else
        {
            inx = def.indexOf("dlu"); //$NON-NLS-1$
            if (inx > -1)
            {
                len = Integer.parseInt(def.substring(0, inx));
                type = SIZE_TYPE.Dlus;
                
            } else
            {
                throw new RuntimeException("Can't parse ["+def+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }
    
    protected void parseMinMax(final String def)
    {
        String[] toks = StringUtils.split(def, "();"); //$NON-NLS-1$
        parseNum(toks[1]);
    }
    
    public void parseAlign(final String def, final boolean isRow)
    {
        
        int inx = def.indexOf(':');
        if (inx > -1)
        {
            String val = def.substring(0, inx);
            if (val.equals("f") || val.equals("fill")) //$NON-NLS-1$ //$NON-NLS-2$
            {
                align = ALIGN_TYPE.Fill;
                
            } else if (isRow)
            {
                if (val.equals("t") || val.equals("top")) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    align = ALIGN_TYPE.Top;
                    
                } else if (val.equals("c") || val.equals("center")) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    align = ALIGN_TYPE.Center;
                    
                } else if (val.equals("b") || val.equals("bottom")) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    align = ALIGN_TYPE.Bottom;
                } else
                {
                    align = ALIGN_TYPE.None;
                }
                    
            } else
            {
                if (val.equals("l") || val.equals("left")) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    align = ALIGN_TYPE.Left;
                    
                } else if (val.equals("c") || val.equals("center")) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    align = ALIGN_TYPE.Center;
                    
                } else if (val.equals("r") || val.equals("right")) //$NON-NLS-1$ //$NON-NLS-2$
                {
                    align = ALIGN_TYPE.Right;
                } else
                {
                    align = ALIGN_TYPE.None;
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getAlignStr(align));
        if (len == -1)
        {
            sb.append("p"); //$NON-NLS-1$
            
        } else if (minMax != MINMAX_TYPE.None)
        {
            sb.append(minMax == MINMAX_TYPE.Min ? "min(" : "max("); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append(len);
            sb.append(type == SIZE_TYPE.Pixels ? "px" : "dlu"); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append(";p)"); //$NON-NLS-1$
            
        } else
        {
            sb.append(len);
            sb.append(type == SIZE_TYPE.Pixels ? "px" : "dlu"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        if (isGrow)
        {
            sb.append(":g"); //$NON-NLS-1$
        }
        
        return sb.toString();
    }
    
    /**
     * (Yes I know I could do all this through the enum)
     * @param align
     * @return
     */
    protected String getAlignStr(@SuppressWarnings("hiding")
    final ALIGN_TYPE align)
    {
        if (align != null)
        {
            switch (align)
            {
                case None   : return ""; //$NON-NLS-1$
                case Fill   : return "f:"; //$NON-NLS-1$
                case Left   : return "l:"; //$NON-NLS-1$
                case Center : return "c:"; //$NON-NLS-1$
                case Right  : return "r:"; //$NON-NLS-1$
                case Top    : return "t:"; //$NON-NLS-1$
                case Bottom : return "b:"; //$NON-NLS-1$
            }
        }
        
        return ""; //$NON-NLS-1$
    }

    /**
     * @return the minMax
     */
    public MINMAX_TYPE getMinMax()
    {
        return minMax;
    }

    /**
     * @param minMax the minMax to set
     */
    public void setMinMax(MINMAX_TYPE minMax)
    {
        this.minMax = minMax;
    }

    /**
     * @return the len
     */
    public int getLen()
    {
        return len;
    }

    /**
     * @param len the len to set
     */
    public void setLen(int len)
    {
        this.len = len;
    }

    /**
     * @return the type
     */
    public SIZE_TYPE getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(SIZE_TYPE type)
    {
        this.type = type;
    }

    /**
     * @return the isGrow
     */
    public boolean isGrow()
    {
        return isGrow;
    }

    /**
     * @param isGrow the isGrow to set
     */
    public void setGrow(boolean isGrow)
    {
        this.isGrow = isGrow;
    }

    /**
     * @return the align
     */
    public ALIGN_TYPE getAlign()
    {
        return align;
    }

    /**
     * @param align the align to set
     */
    public void setAlign(ALIGN_TYPE align)
    {
        this.align = align;
    }

    /**
     * @return the isInUse
     */
    public boolean isInUse()
    {
        return isInUse;
    }

    /**
     * @param isInUse the isInUse to set
     */
    public void setInUse(boolean isInUse)
    {
        this.isInUse = isInUse;
    }

    /**
     * @return the isPreferredSize
     */
    public boolean isPreferredSize()
    {
        return isPreferredSize;
    }

    /**
     * @param isPreferredSize the isPreferredSize to set
     */
    public void setPreferredSize(boolean isPreferredSize)
    {
        this.isPreferredSize = isPreferredSize;
    }
}
