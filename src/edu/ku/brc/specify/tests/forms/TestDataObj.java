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
package edu.ku.brc.specify.tests.forms;

import java.util.Set;

import edu.ku.brc.ui.IconManager;

public class TestDataObj
{

    protected String  textField = "Text Field Data";
    protected String  textArea  = "Text Area Data\r\nLine 2\n\rLine 3";
    protected boolean checkBox  = true;
    protected String  comboBox  = "Item 1";
    protected String  imagePath = "";
    protected String  imagePathURL = "http://localhost/Luery_u0.jpg";
    protected String  browseField  = "this needs to be a path";
    protected java.util.Date date;
    protected String  noImage = "";
    protected String  stateCode;
    
    @SuppressWarnings("unchecked")
    protected Set subObjects;

    public TestDataObj()
    {
        imagePath = IconManager.getImagePath("../tests/forms/Luery_u0_large.jpg").toString();
        date = new java.util.Date();
    }

    @SuppressWarnings("unchecked")
    public Set getSubObjects() {
        return this.subObjects;
    }
    
    @SuppressWarnings("unchecked")
    public void setSubObjects(Set subObjects) {
        this.subObjects = subObjects;
    }
    
    public boolean isCheckBox()
    {
        return checkBox;
    }


    public void setCheckBox(boolean checkBox)
    {
        this.checkBox = checkBox;
    }


    public String getComboBox()
    {
        return comboBox;
    }


    public void setComboBox(String comboBox)
    {
        this.comboBox = comboBox;
    }


    public String getImagePath()
    {
        return imagePath;
    }


    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }


    public String getTextArea()
    {
        return textArea;
    }


    public void setTextArea(String textArea)
    {
        this.textArea = textArea;
    }


    public String getTextField()
    {
        return textField;
    }


    public void setTextField(String textField)
    {
        this.textField = textField;
    }


    public String getBrowseField()
    {
        return browseField;
    }


    public void setBrowseField(String browseField)
    {
        this.browseField = browseField;
    }


    public java.util.Date getDate()
    {
        return date;
    }


    public void setDate(java.util.Date date)
    {
        this.date = date;
    }


    public String getImagePathURL()
    {
        return imagePathURL;
    }


    public void setImagePathURL(String imagePathURL)
    {
        this.imagePathURL = imagePathURL;
    }


    public String getNoImage()
    {
        return noImage;
    }


    public void setNoImage(String noImage)
    {
        this.noImage = noImage;
    }

    public String getStateCode()
    {
        return stateCode;
    }

    public void setStateCode(String stateCode)
    {
        this.stateCode = stateCode;
    }

}
