/**
 * 
 */
package edu.ku.brc.specify.utilapps.morphbank;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectionObject;

/**
 * @author timo
 *
 */
public class SDStateFileNameParser implements FileNameParserIFace
{
	public static final int SpecPic = 0;
	public static final int DorsalPic = 1;
	public static final int VentralPic = 2;
	
	protected String prefix = "Picture Files/Specimen Pics/";
	
	protected int picType;
	protected String srchFieldName;
	protected String suffix;
	
	protected Connection testConnection;
	
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getRecordIds(java.lang.String)
	 */
	@Override
	public List<Integer> getRecordIds(String fileName) 
	{
		List<Integer> result = new Vector<Integer>();
		String id = fileName.replace("_", "");
		id = prefix + fileName;
		String sql = "select CollectionObjectID from collectionobject co inner join collectionobjectattribute coa "
			+ "on coa.collectionobjectattributeid = co.collectionobjectattributeid where coa."
			+ srchFieldName + " = '" + id + "'";
		//System.out.println(sql);
		//result.add(1);
		Connection conn = testConnection != null ? testConnection : DBConnection.getInstance().getConnection();
		Vector<Object> idObjs = BasicSQLUtils.querySingleCol(conn, sql);
		if (idObjs != null)
		{
			for (Object idObj : idObjs)
			{
				result.add((Integer )idObj);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.utilapps.morphbank.FileNameParserIFace#getTableId()
	 */
	@Override
	public Integer getTableId() 
	{
		return CollectionObject.getClassTableId();
	}

	
	/**
	 * @return the testConnection
	 */
	public Connection getTestConnection() 
	{
		return testConnection;
	}

	/**
	 * @param testConnection the testConnection to set
	 */
	public void setTestConnection(Connection testConnection) 
	{
		this.testConnection = testConnection;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		String picDirName = "/media/Terror/ConversionsAndFixes/sdstate/SpSub31OCT10/PictureFiles/SpecimenPics";
		String[] exts = {"jpg", "JPG"};
		try
		{
		    BatchAttachFiles      batchAttachFiles = new BatchAttachFiles();
			File                  picDir           = new File(picDirName);
			Vector<File>          files            = batchAttachFiles.bldFilesFromDir(picDir, exts);
			SDStateFileNameParser p                = new SDStateFileNameParser();
			
			String connStr = "jdbc:mysql://localhost/sdstate6202?characterEncoding=UTF-8&autoReconnect=true"; 
			Connection conn = DriverManager.getConnection(connStr, "Master", "Master");
			p.setTestConnection(conn);
			p.setPicType(SpecPic);
			int fileCount = 0;
			int filesOfTypeCount = 0;
			int idCount = 0;
			for (File f : files)
			{
				System.out.print(f.getName() + " - ");
				if (p.looksLikePicOfCurrentType(f.getName()))
				{
					filesOfTypeCount++;
				}
				List<Integer> ids = p.getRecordIds(f.getName());
				for (Integer id : ids)
				{
					System.out.print(id + ", ");
					idCount++;
				}
				System.out.println();
				fileCount++;
			}
			System.out.println("files: " + fileCount + ", files of type: " + filesOfTypeCount + ", ids: " + idCount);
			
			//Now see how many files are missing
			
			//Spec files
			String sql = "select distinct text3 from collectionobjectattribute "
				+ "where text3 is not null order by 1";
			Vector<Object> fileNamesFromDB = BasicSQLUtils.querySingleCol(conn, sql);
			Vector<String> missingFiles = new Vector<String>();
			for (Object dbFileNameObj : fileNamesFromDB)
			{
				String fileName = ((String )dbFileNameObj).replace("Picture Files/Specimen Pics/", "");
				boolean fileExists = false;
				for (File f : files)
				{
					if (f.getName().equalsIgnoreCase(fileName))
					{
						fileExists = true;
						break;
					}
				}
				if (!fileExists)
				{
					missingFiles.add(fileName);
				}				
			}
			
			//Dorsal files
			sql = "select distinct text1 from collectionobjectattribute "
				+ "where text1 is not null order by 1";
			fileNamesFromDB = BasicSQLUtils.querySingleCol(conn, sql);
			for (Object dbFileNameObj : fileNamesFromDB)
			{
				String fileName = ((String )dbFileNameObj).replace("Picture Files/Specimen Pics/", "");
				boolean fileExists = false;
				for (File f : files)
				{
					if (f.getName().equalsIgnoreCase(fileName))
					{
						fileExists = true;
						break;
					}
				}
				if (!fileExists)
				{
					missingFiles.add(fileName);
				}				
			}

			//Ventral files
			sql = "select distinct text2 from collectionobjectattribute "
				+ "where text2 is not null order by 1";
			fileNamesFromDB = BasicSQLUtils.querySingleCol(conn, sql);
			for (Object dbFileNameObj : fileNamesFromDB)
			{
				String fileName = ((String )dbFileNameObj).replace("Picture Files/Specimen Pics/", "");
				boolean fileExists = false;
				for (File f : files)
				{
					if (f.getName().equalsIgnoreCase(fileName))
					{
						fileExists = true;
						break;
					}
				}
				if (!fileExists)
				{
					missingFiles.add(fileName);
				}				
			}
			
			System.out.println("There are " + missingFiles.size() + " missing files: ");
			for (String mf : missingFiles)
			{
				System.out.println(mf);
			}
			FileUtils.writeLines(new File("/media/Terror/ConversionsAndFixes/sdstate/MissingFiles.txt"), missingFiles);
			
			
			//Now a check - after batch attach - to see what files were not attached to any records.
			sql = "select distinct Title from attachment order by 1";
			Vector<Object> filesAttached = BasicSQLUtils.querySingleCol(conn, sql);
			Vector<String> filesNotAttached = new Vector<String>();
			for (File f : files)
			{
				String fName = f.getName();
				boolean fileWasAttached = false;
				for (Object obj : filesAttached)
				{
					if (fName.equalsIgnoreCase(obj.toString()))
					{
						fileWasAttached = true;
						break;
					}
				}
				if (!fileWasAttached)
				{
					filesNotAttached.add(fName);
				}
			}
			System.out.println("Files that were not attached:");
			for (String fna : filesNotAttached)
			{
				System.out.println(fna);
			}
			FileUtils.writeLines(new File("/media/Terror/ConversionsAndFixes/sdstate/UnattachedFiles.txt"), filesNotAttached);
		} catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(-1);
		}

	}

	/**
	 * @return the prefix
	 */
	public String getPrefix() 
	{
		return prefix;
	}

	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) 
	{
		this.prefix = prefix;
	}

	/**
	 * @return the picType
	 */
	public int getPicType() 
	{
		return picType;
	}

	/**
	 * @param fileName
	 * 
	 * mostly for testing and data-checking
	 */
	public boolean looksLikePicOfCurrentType(String fileName)
	{
		return fileName.endsWith(suffix + ".jpg")
		 	|| fileName.endsWith(suffix + " .jpg")
		 	|| fileName.endsWith(suffix + ".JPG")
		 	|| fileName.endsWith(suffix + " .JPG")
		 	|| fileName.endsWith(suffix.toLowerCase() + ".jpg")
		 	|| fileName.endsWith(suffix.toLowerCase() + " .jpg")
		 	|| fileName.endsWith(suffix.toLowerCase() + ".JPG")
		 	|| fileName.endsWith(suffix.toLowerCase() + " .JPG");
	}
	/**
	 * @param picType the picType to set
	 */
	public void setPicType(int picType) 
	{
		this.picType = picType;
		if (picType == SpecPic)
		{
			srchFieldName = "Text3";
			suffix = "S";
		} else if (picType == DorsalPic)
		{
			srchFieldName = "Text1";
			suffix = "D";
		} else 
		{
			srchFieldName = "Text2";
			suffix = "V";
		}
	}

	
}
