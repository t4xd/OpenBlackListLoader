package mc.ta3.obll;

import java.util.ArrayList;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.yaml.snakeyaml.Yaml;

public class OBLUtil {

	public static ArrayList<Map<String, Object>> returnMapGet(String s) throws org.json.simple.parser.ParseException {
		Yaml yaml = new Yaml();
		JSONParser parser = new JSONParser();
		String prettyJSONString = parser.parse(s).toString();
		ArrayList<Map<String, Object>> map = (ArrayList<Map<String, Object>>) yaml.load(prettyJSONString);
		return map;
	}

}
