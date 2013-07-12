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
package edu.ku.brc.specify.tasks.subpane.wb.schema;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * Stores information on database relationships for use in workbench uploads.
 */
public class Relationship
{
	public static final String REL_ONETOMANY = "OneToMany";
	
    /**
     * The field on the 'from' side of the relatinoship.
     */
    protected Field  field;
    /**
     * The field on the 'to' side of the relatinoship.
     */
    protected Field  relatedField;
    /**
     * The relationship type.
     */
    protected String relType;

    /**
     * @param field
     * @param relatedField
     * @param relType
     */
    public Relationship(Field field, Field relatedField, String relType)
    {
        super();
        this.field = field;
        this.relatedField = relatedField;
        this.relType = relType;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object relObject)
    {
        if (relObject == null)
            return false;

        if (this.getClass() != relObject.getClass())
            return false;

        Relationship aRel = (Relationship) relObject;
        return this.field.getName().equals(aRel.getField().getName())
                && this.field.getTable().getName().equals(aRel.getField().getTable().getName())
                && this.relatedField.getName().equals(aRel.getRelatedField().getName())
                && this.relatedField.getTable().getName().equals(
                        aRel.getRelatedField().getTable().getName());
    }

    /**
     * @return the field
     */
    public final Field getField()
    {
        return field;
    }

    /**
     * @param field the field to set
     */
    public final void setField(Field field)
    {
        this.field = field;
    }

    /**
     * @return the relatedField
     */
    public final Field getRelatedField()
    {
        return relatedField;
    }

    /**
     * @param relatedField the relatedField to set
     */
    public final void setRelatedField(Field relatedField)
    {
        this.relatedField = relatedField;
    }

    /**
     * @return the relType
     */
    public final String getRelType()
    {
        return relType;
    }

    /**
     * @param relType the relType to set
     */
    public final void setRelType(String relType)
    {
        this.relType = relType;
    }

    /**
     * @param rel
     * @return -1 if this relationship is less than rel, 0 if equal, 1 if this relationship is >
     *         rel.
     */
    public int compareTo(Relationship rel)
    {
        int result = field.getName().compareTo(rel.getField().getName());
        if (result != 0) { return result; }
        result = relatedField.getName().compareTo(rel.getField().getName());
        if (result != 0) { return result; }
        return relType.compareTo(rel.getRelType());
    }
}
