package edu.ku.brc.specify.tools.datamodelgenerator;
/**
 * Create primary key data.
 *
 * @code_status Alpha
 * 
 * @author megkumin
 *
 */
public class Id extends Field
{
    
    /**
     * @param name the name of the field	
     * @param type the type of the field
     * @param column the name of the column this field maps to in the database
     * @param length the length of the field
     */
	public Id(String name, String type, String column, String length)
	{
		super(name,type,column, length);
	}
}
