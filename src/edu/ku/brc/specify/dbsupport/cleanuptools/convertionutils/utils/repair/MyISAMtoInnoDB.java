/**
 * 
 */
package utils.repair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class MyISAMtoInnoDB {

	
	public static List<Pair<String, List<String>>> getFKs(File inFile) throws IOException {
		List<?> in = FileUtils.readLines(inFile);
		List<Pair<String, List<String>>> tbls = new ArrayList<Pair<String, List<String>>>();
		String currentTbl = "";
		List<String> currentFKs = new ArrayList<String>();
		for (Object l : in) {
			String line = l.toString();
			if (line.startsWith("CREATE TABLE")) {
				if (!"".equals(currentTbl)) {
					tbls.add(new Pair<String, List<String>>(currentTbl, currentFKs));
				}
				currentTbl = line.split("`")[1];
				currentFKs = new ArrayList<String>();
			} else if (line.contains("FOREIGN KEY")) {
				currentFKs.add(line);
			}
		}
		if (!"".equals(currentTbl) && currentFKs.size() > 0) {
			tbls.add(new Pair<String, List<String>>(currentTbl, currentFKs));
		}
		return tbls;
	}
	
	public static void generateFKScript(File inFile, File outFile) throws IOException {
		List<Pair<String, List<String>>> fks = getFKs(inFile);
		for (Pair<String, List<String>> fk : fks) {
			System.out.println(fk.getFirst());
			for (String f : fk.getSecond()) {
				System.out.println("    " + f);
			}
		}
		List<String> result = new ArrayList<String>();

		//set engine to innodb
		for (Pair<String, List<String>> fk : fks) {
			result.add("ALTER TABLE `" + fk.getFirst() + "` ENGINE=InnoDB;");
		}
		result.add("#"); result.add("#"); result.add("#"); //MYSQL comments
		for (Pair<String, List<String>> fk : fks) {
			if (fk.getSecond().size() > 0) {
				result.add("ALTER TABLE `" + fk.getFirst() + "` ");
				for (String f : fk.getSecond()) {
					result.add("    ADD " + f);
				}
				result.add(";");
				result.add("#"); //MYSQL comment
			}
		}
		
		FileUtils.writeLines(outFile, result);
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			generateFKScript(new File("D:/data/uta/nodata.sql"), new File("D:/data/uta/fkscript_2.sql"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
