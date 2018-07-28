package mc.ta3.obll;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

public class BlackListPackage {

	/*
	 *   [ BLACK LIST PACKAGE ]
	 *
	 *   BlackListをインスタンス化して保存する
	 */

	private String name = "";
	private String address = "";
	private String action = "";
	private ArrayList<Map<String, Object>> res;
	private HashMap<UUID, FileConfiguration> fg = new HashMap<>();
	private String kickmessage = "";

	public BlackListPackage(String name, String addr, String action, String kickmessage) {
		this.name = name;
		this.address = addr;
		this.action = action;
		this.kickmessage = kickmessage;
		Bukkit.getScheduler().runTaskAsynchronously(OBLLoader.i, new Runnable() {
			@Override
			public void run() {
				try {
					Map<String, Object> s = null; // 下ごしらえ
					URL url = new URL(address);
					// エージェント設定してSSLの403回避
					HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
					httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
					InputStream in = httpConn.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(in, "SHIFT-JIS"));
					StringBuffer lines = new StringBuffer(4096); // バッファ多めに
					while (true) { // 読み取る
						String line = reader.readLine();
						if (line == null) {
							break;
						}
						lines.append(line);
						lines.append('\n');
					}
					res = OBLUtil.returnMapGet(lines.toString()); // 取り敢えずMapに変換する
					for(Map<String, Object> t : res){
						// JSONだいきらいだからYAMLにする
						FileConfiguration c = new YamlConfiguration();
						String sx = new Yaml().dump(t);
						c.loadFromString(sx); // YAMLになった
						fg.put(UUID.fromString(c.getString("uuid_format")), c); // UUIDをKeyにしてそのまんまキープ
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getAction() {
		return action;
	}

	public String getKickMessage() {
		return kickmessage;
	}

	public FileConfiguration check(UUID u) {
		if(!fg.containsKey(u)) {
			return null;
		}
		FileConfiguration fc = fg.get(u);
		return fc;
	}

}
