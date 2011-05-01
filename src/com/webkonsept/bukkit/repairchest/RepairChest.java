package com.webkonsept.bukkit.repairchest;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class RepairChest extends JavaPlugin {
	private boolean verbose = false; // SET TO FALSE FOR RELEASE, YEAH?!
	private Logger log = Logger.getLogger("Minecraft");
	private PermissionHandler Permissions;
	private boolean usePermissions;
	private RepairChestPlayerListener playerListener = new RepairChestPlayerListener(this);
	private RepairChestBlockListener blockListener = new RepairChestBlockListener(this);
	private HashMap<String,Boolean> fallbackPermissions = new HashMap<String,Boolean>();
	
	@Override
	public void onDisable() {
		this.out("Disabled");
		// TODO Add plugin.isEnabled() check to things...
	}

	@Override
	public void onEnable() {
		if(!setupPermissions()){
			fallbackPermissions.put("repairchest.create",false);
			fallbackPermissions.put("repairchest.use",true);
			fallbackPermissions.put("repairchest.destroy",false);
		}
		PluginManager pm =getServer().getPluginManager();
		pm.registerEvent(Event.Type.SIGN_CHANGE,blockListener,Priority.Normal,this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT,playerListener,Priority.Normal,this);
	}
	public boolean permit(Player player,String permission){ 
		this.babble(player.getName()+" asked permission to "+permission);
		boolean allow = false; // Default to GTFO
		if ( usePermissions ){
			this.babble("Permissions in use");
			if (Permissions.has(player,permission)){
				this.babble("Go ahead!");
				allow = true;
			}
			else {
				this.babble("Permission DUHNIIIIED!!!");
			}
		}
		else if (player.isOp()){
			this.babble("Go ahead, operator! (Fallback permission)");
			allow = true;
		}
		else {
			if (fallbackPermissions.get(permission) || false){
				this.babble("Go ahead! (Fallback permission)");
				allow = true;
			}
			else{
				this.babble("Forget it!");
			}
		}
		return allow;
	}
	private boolean setupPermissions() {
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
		if (this.Permissions == null){
			if (test != null){
				this.Permissions = ((Permissions)test).getHandler();
				this.usePermissions = true;
				return true;
			}
			else {
				this.out("Permissions plugin not found, defaulting to OPS CHECK mode");
				return false;
			}
		}
		else {
			this.out("Urr, this is odd...  Permissions are already set up!");
			return true;
		}
	}
	public void out(String message) {
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + "] " + message);
	}
	public void crap(String message){
		PluginDescriptionFile pdfFile = this.getDescription();
		log.severe("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + " CRAP] " + message);
	}
	public void babble(String message){
		if (!this.verbose){ return; }
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + " VERBOSE] " + message);
	}

}
