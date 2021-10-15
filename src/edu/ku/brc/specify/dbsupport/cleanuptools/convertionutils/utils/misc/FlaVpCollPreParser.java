package utils.misc;

import java.sql.Connection;

public class FlaVpCollPreParser extends Restringer {

	public FlaVpCollPreParser() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String restring(String str, Connection con) {
		char[] chars = str.toCharArray();
		int commas = 0;
		String result = "";
		for (char c : chars) {
			char next = c;
			if (',' == next) {
				if (++commas % 2 == 0) {
					next = ';';
				}
			} else if ('&' == next) {
				commas = 0;
			}
			result += next;
		}
		return result;
	}

}
