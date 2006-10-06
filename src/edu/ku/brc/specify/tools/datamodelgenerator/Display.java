/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.specify.tools.datamodelgenerator;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 */
public class Display
{
    protected String objtitle; 
    protected String view; 
    protected String dataobjformatter; 
    protected String uiformatter;
    
    public Display(String objtitle, String view, String dataobjformatter, String uiformatter)
    {
        super();
        this.objtitle         = objtitle;
        this.view             = view;
        this.dataobjformatter = dataobjformatter;
        this.uiformatter      = uiformatter;
    }
    
    public String getDataobjformatter()
    {
        return dataobjformatter;
    }
    public String getObjtitle()
    {
        return objtitle;
    }
    public String getUiformatter()
    {
        return uiformatter;
    }
    public String getView()
    {
        return view;
    }
    
    
}
