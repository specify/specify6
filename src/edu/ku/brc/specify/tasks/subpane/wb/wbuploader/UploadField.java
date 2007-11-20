package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.lang.reflect.Method;

import edu.ku.brc.specify.tasks.subpane.wb.schema.Field;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Relationship;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 * Describes properties of fields being uploaded as part of a workbench upload.
 */
public class UploadField
{
    /**
     * The Field in the database being uploaded to.
     */
    protected Field        field;
    /**
     * The current value of the field. (The text contained in the Workbench)
     */
    protected String       value;
    /**
     * For UploadFields implementing foreign keys: The relationship the foreign key participates in.
     */
    protected Relationship relationship;
    /**
     * The field's column index in the workbench being uploaded. (-1 when not applicable (as for
     * foreign keys.))
     */
    protected int          index;
    /**
     * The caption of the field's column in the workbench. (null when index == -1)
     */
    protected String       wbFldName;
    /**
     * The one to many 'order' of the field (e.g. LastName1, lastName2, ... )
     */
    protected Integer      sequence = 0;
    /**
     * True if the field must contain data
     */
    protected boolean      required;
    /**
     * The method used to set the field's contents to the java object representing the field's
     * Table.
     */
    protected Method       setter;

    /**
     * @return the setter
     */
    public final Method getSetter()
    {
        return setter;
    }

    /**
     * @param setter the setter to set
     */
    public final void setSetter(Method setter)
    {
        this.setter = setter;
    }

    public UploadField(Field field, int index, String wbFldName, Relationship relationship)
    {
        this.field = field;
        this.index = index;
        this.wbFldName = wbFldName;
        this.relationship = relationship;
        this.required = false;
    }

    @Override
    public String toString()
    {
        return wbFldName + ", " + String.valueOf(index) + ", " + field.getName();
    }
    /**
     * @return the field
     */
    public Field getField()
    {
        return field;
    }

    /**
     * @param field the field to set
     */
    public void setField(Field field)
    {
        this.field = field;
    }

    /**
     * @param val
     * @return true if val is a valid value for this field.
     */
    @SuppressWarnings("unused")
    public boolean validate(final String val)
    {
        return true;
    }

    /**
     * @return the value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * @return the relationship
     */
    public Relationship getRelationship()
    {
        return relationship;
    }

    /**
     * @param relationship the relationship to set
     */
    public void setRelationship(Relationship relationship)
    {
        this.relationship = relationship;
    }

    /**
     * @return the sequence
     */
    public Integer getSequence()
    {
        return sequence;
    }

    /**
     * @param sequence the sequence to set
     */
    public void setSequence(Integer sequence)
    {
        this.sequence = sequence;
    }

    /**
     * @return the index
     */
    public int getIndex()
    {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index)
    {
        this.index = index;
    }

    /**
     * @return the required
     */
    public boolean isRequired()
    {
        return required;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(boolean required)
    {
        this.required = required;
    }

    /**
     * @return the wbFldName
     */
    public final String getWbFldName()
    {
        return wbFldName;
    }
}
