/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
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
    private String[]     prefixes    = {"f", "fill", "l", "left", "r", "right", "c", "center", "t", "top", "b", "bottom"};
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
            def = "p";
        } else
        {
            def = defArg.toLowerCase();
        }
        
        if (def.startsWith("min"))
        {
            minMax = MINMAX_TYPE.Min;
            parseMinMax(def);
            
        } else if (def.startsWith("max"))
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
        isGrow = def.endsWith(":g") || def.endsWith(":grow");
    }
    
    protected void parseNum(final String def)
    {
        int inx = def.indexOf("px");
        if (inx > -1)
        {
            len = Integer.parseInt(def.substring(0, inx));
            type = SIZE_TYPE.Pixels;
        } else
        {
            inx = def.indexOf("dlu");
            if (inx > -1)
            {
                len = Integer.parseInt(def.substring(0, inx));
                type = SIZE_TYPE.Dlus;
                
            } else
            {
                throw new RuntimeException("Can't parse ["+def+"]");
            }
        }
    }
    
    protected void parseMinMax(final String def)
    {
        String[] toks = StringUtils.split(def, "();");
        parseNum(toks[1]);
    }
    
    public void parseAlign(final String def, final boolean isRow)
    {
        
        int inx = def.indexOf(':');
        if (inx > -1)
        {
            String val = def.substring(0, inx);
            if (val.equals("f") || val.equals("fill"))
            {
                align = ALIGN_TYPE.Fill;
                
            } else if (isRow)
            {
                if (val.equals("t") || val.equals("top"))
                {
                    align = ALIGN_TYPE.Top;
                    
                } else if (val.equals("c") || val.equals("center"))
                {
                    align = ALIGN_TYPE.Center;
                    
                } else if (val.equals("b") || val.equals("bottom"))
                {
                    align = ALIGN_TYPE.Bottom;
                } else
                {
                    align = ALIGN_TYPE.None;
                }
                    
            } else
            {
                if (val.equals("l") || val.equals("left"))
                {
                    align = ALIGN_TYPE.Left;
                    
                } else if (val.equals("c") || val.equals("center"))
                {
                    align = ALIGN_TYPE.Center;
                    
                } else if (val.equals("r") || val.equals("right"))
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
            sb.append("p");
            
        } else if (minMax != MINMAX_TYPE.None)
        {
            sb.append(minMax == MINMAX_TYPE.Min ? "min(" : "max(");
            sb.append(len);
            sb.append(type == SIZE_TYPE.Pixels ? "px" : "dlu");
            sb.append(";p)");
            
        } else
        {
            sb.append(len);
            sb.append(type == SIZE_TYPE.Pixels ? "px" : "dlu");
        }
        
        if (isGrow)
        {
            sb.append(":g");
        }
        
        return sb.toString();
    }
    
    /**
     * (Yes I know I could do all this through the enum)
     * @param align
     * @return
     */
    protected String getAlignStr(final ALIGN_TYPE align)
    {
        if (align != null)
        {
            switch (align)
            {
                case None   : return "";
                case Fill   : return "f:";
                case Left   : return "l:";
                case Center : return "c:";
                case Right  : return "r:";
                case Top    : return "t:";
                case Bottom : return "b:";
            }
        }
        
        return "";
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