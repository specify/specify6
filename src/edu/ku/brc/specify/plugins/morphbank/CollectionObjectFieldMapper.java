/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Vector;

import javax.xml.namespace.QName;

import net.morphbank.mbsvc3.xml.ObjectFactory;
import net.morphbank.mbsvc3.xml.XmlBaseObject;

/**
 * @author timo
 *
 * @code_status Alpha
 *
 * Apr 7, 2010
 */
public class CollectionObjectFieldMapper
{
	protected Integer collectionObjectId;
	protected DwcMapper dwcMapper;
	protected Vector<QName> morphBankDwcQNames;
	
	
	/**
	 * @param collectionObjectId
	 * @throws Exception
	 */
	public CollectionObjectFieldMapper(Integer collectionObjectId) throws Exception
	{
		this.collectionObjectId = collectionObjectId;
		buildQNames();
		dwcMapper = getDwcMapper();
	}
	
	/**
	 * @return the DarwinCore appropriate mappings for the current context.
	 * 
	 * Possibly lots and lots of work to do here.
	 */
	protected DwcMapper getDwcMapper()
	{
		return new DwcMapper(1);
	}
	
	
	/**
	 * @throws Exception
	 * 
	 * Builds a list of QNames declared in MorphBank's ObjectFactory.
	 * 
	 * Probably will eventually not be necessary.
	 */
	protected void buildQNames() throws Exception
	{
		Class<?> ojClass = ObjectFactory.class;
		Field[] flds = ojClass.getFields();
		morphBankDwcQNames = new Vector<QName>();
		for (Field fld : flds)
		{
			int mod = fld.getModifiers();
			if (fld.getType().equals(javax.xml.namespace.QName.class)
					&& Modifier.isFinal(mod) 
					&& Modifier.isStatic(mod)
					&& Modifier.isPublic(mod))
			{
				morphBankDwcQNames.add((QName )fld.get(null));
			}
		}
	}
	
	
	public void setXmlSpecimenFields(XmlBaseObject xmlSpec) throws Exception
	{
//		DarwinCoreSpecimen spec = new DarwinCoreSpecimen(dwcMapper);
//		spec.setCollectionObjectId(collectionObjectId);
//		for (Pair<String, Object> fld : spec.getFieldValues())
//		{
//			return new JAXBElement<String>(_InstitutionCode_QNAME, String.class,
//					null, value);
//		}
	}

	public void setXmlImageFields(XmlBaseObject xmlSpec)
	{
		
	}
	
	/**
	 * @return the collectionObjectId
	 */
	public Integer getCollectionObjectId() 
	{
		return collectionObjectId;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		

	}

}
