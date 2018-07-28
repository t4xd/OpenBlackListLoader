package mc.ta3.obll;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class OBLLoader extends JavaPlugin implements CommandExecutor,Listener{

	protected static OBLLoader i;

	final String ver = "1.0.0";

	private static ArrayList<BlackListPackage> blp = new ArrayList<>();
	private static boolean protector = true;

	@Override
	public void onEnable() {
		// OPEN BLACK LIST LOADER STARTUP //
		i = this;
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN+"OpenBlackListLoader v"+ver);
		getCommand("obl").setExecutor(this);
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN+Prefix.INFO+"Importing OpenBlackList");
		File f = new File("plugins/OpenBlackListLoader/config.yml");
		if(!f.exists()) {

		}
		FileConfiguration r = YamlConfiguration.loadConfiguration(f);
		if(r.contains("BlackList")) {
			ConfigurationSection cs = r.getConfigurationSection("BlackList");
			// ロード処理
			for(String s : cs.getKeys(false)) {
				String name = s;
				String addr = cs.getString(s+".Address");
				String action = cs.getString(s+".Action");
				String kickmes = ChatColor.WHITE+"あなたはOpenBlackListに掲載されているため接続できません";
				if(cs.contains(s+"KickMessage")) {
					kickmes = cs.getString(s+"KickMessage");
				}
				// BlackListPackageのListに追加する
				blp.add(new BlackListPackage(name, addr,action, kickmes));
				Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN+Prefix.INFO+"ADDR: "+addr+" ACTION: "+action);
			}
		}
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN+Prefix.INFO+"DONE");
	}

	@Override
	public void onDisable() {
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN+"OpenBlackListLoader Disabled");
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		boolean found = false;
		// 検索する
		for(BlackListPackage bp : blp) {
			FileConfiguration fc = bp.check(p.getUniqueId());
			if(fc != null) {
				found = true;
				// KICKの場合
				if(bp.getAction().equals("KICK")) {
					// 指定されたKICK MESSAGEで蹴る
					p.kickPlayer(bp.getKickMessage());
					// 通知する
					Bukkit.broadcast(Prefix.WARN+ChatColor.RED+"OpenBlackList("+bp.getName()+")に登録されているプレイヤーです - "+p.getName(), "obl.notify");
					Bukkit.broadcast(Prefix.WARN+ChatColor.RED+""+p.getName()+"を自動Kickしました。", "obl.notify");
					return;
				}
				// NOTIFYの場合
				if(bp.getAction().equals("NOTIFY")) {
					// 通知する
					Bukkit.broadcast(Prefix.WARN+"OpenBlackList("+bp.getName()+")に登録されているプレイヤーです - "+p.getName(), "obl.notify");
				}
			}
		}
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, final String[] args) {
		if(cmd.getName().equals("obl")) {
			if(args.length == 1 && args[0].equals("list")) { // 登録中のBlackListを返す
				sender.sendMessage(Prefix.INFO+"======= [ BlackList ] =======");
				for(BlackListPackage bp : blp) {
					sender.sendMessage(Prefix.INFO+""+bp.getName()+" - "+bp.getAddress()+" - "+bp.getAction());
				}
				return true;
			}
			if(args.length == 1 && args[0].equals("reload")) { // 再読み込みする
				blp.clear();
				sender.sendMessage(ChatColor.GREEN+Prefix.OK+"Reloading OpenBlackList...");
				File f = new File("plugins/OpenBlackListLoader/config.yml");
				FileConfiguration r = YamlConfiguration.loadConfiguration(f);
				// この辺onEnable()時の処理と同じ
				if(r.contains("BlackList")) {
					ConfigurationSection cs = r.getConfigurationSection("BlackList");
					for(String s : cs.getKeys(false)) {
						String name = s;
						String addr = cs.getString(s+".Address");
						String action = cs.getString(s+".Action");
						String kickmes = ChatColor.WHITE+"あなたはOpenBlackListに掲載されているため接続できません";
						if(cs.contains(s+"KickMessage")) {
							kickmes = cs.getString(s+"KickMessage");
						}
						blp.add(new BlackListPackage(name, addr,action, kickmes));
						Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN+Prefix.INFO+"ADDR: "+addr+" ACTION: "+action);
					}
				}
				sender.sendMessage(ChatColor.GREEN+Prefix.OK+"DONE");
				return true;
			}
			if(args.length == 2 && args[0].equals("lookup")) { // 検索する
				boolean found = false;
				for(BlackListPackage bp : blp) {
					FileConfiguration fc = bp.check(UUID.fromString(args[1]));
					if(fc != null) {
						found = true;
						sender.sendMessage(ChatColor.DARK_GRAY+"======= [ "+args[1]+" ] =======");
						sender.sendMessage(ChatColor.GRAY+"BLACKLIST: "+bp.getName());
						sender.sendMessage(ChatColor.GRAY+"RECORDED MCID: "+fc.getString("mcid"));
						sender.sendMessage(ChatColor.GRAY+"UUID_FORMAT: "+fc.getString("uuid_format"));
						sender.sendMessage(ChatColor.GRAY+"UUID: "+fc.getString("uuid"));
						sender.sendMessage(ChatColor.GRAY+"DISCORD: "+fc.getString("discord"));
						sender.sendMessage(ChatColor.GRAY+"REASON: "+fc.getString("reason"));
					}
				}
				if(!found) {
					sender.sendMessage(Prefix.ERROR+"NOT FOUND");
				}
				return true;
			}
			sender.sendMessage(Prefix.INFO+"使用法: /obl <list/reload/lookup> [UUID]");
		}
		return true;
	}

}
