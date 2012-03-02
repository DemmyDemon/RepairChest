package com.webkonsept.bukkit.repairchest;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.webkonsept.bukkit.repairchest.listeners.RepairChestBlockListener;
import com.webkonsept.bukkit.repairchest.listeners.RepairChestEntityListener;
import com.webkonsept.bukkit.repairchest.listeners.RepairChestPlayerListener;
import com.webkonsept.bukkit.repairchest.storage.RepairChestList;

public class RepairChestPlugin extends JavaPlugin {
	
	private Logger log = Logger.getLogger("Minecraft");
	
	private RepairChestPlayerListener playerListener = new RepairChestPlayerListener(this);
	public RepairChestBlockListener blockListener = new RepairChestBlockListener(this);
	private RepairChestEntityListener entityListener = new RepairChestEntityListener(this);
	public RepairChestList chestList = new RepairChestList(new File(getDataFolder(),"chests.txt"),this);
	
	public Integer currency = 266; // Gold Ingot
	public Material currencyMaterial = Material.GOLD_INGOT;
	public String currencyName ="g";
	public double baseCost = 0.01; // 100 damage = 1 this.currency
	public boolean verbose = false;
	public boolean partialRepair = false;
	public boolean distributePartialRepair = true;
	public String currencyString = "???";
	public String triggerString = "[Repair]";
	
	private KonseptConfig cfg;
	
	public KonseptConfig cfg(){
	    return cfg;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		boolean success = true;
		if (! this.isEnabled()) return false;
		
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
		}
		
		
		if (command.getName().equalsIgnoreCase("repairchest")){
			if (args.length == 1){
				if (player == null || permit(player,"repairchest.command."+args[0])){
					if (args[0].equalsIgnoreCase("test")){
						if (player == null){
							sender.sendMessage(cfg.tr("testNoConsole"));
						}
						else {
							ItemStack inHand = player.getItemInHand();
							if (inHand.getType().equals(Material.AIR)){
								sender.sendMessage(ChatColor.RED+cfg.tr("testNoEmptyHand"));
							}
							else if (inHand.getMaxStackSize() == 1 && inHand.getType().getMaxDurability() > 10){
								inHand.setDurability((short) (inHand.getType().getMaxDurability() - 5));
								sender.sendMessage(ChatColor.GREEN+cfg.tr("testNearlyBroken"));
							}
							else {
								sender.sendMessage(ChatColor.RED+cfg.tr("testNotSuitable"));
							}
						}
					}
					else if (args[0].equalsIgnoreCase("reload")){
						loadConfig();
						sender.sendMessage(ChatColor.GREEN+cfg.tr("reloadedSuccessfully"));
					}
				}
				else {
					sender.sendMessage(ChatColor.RED+cfg.tr("permissionDenied"));
				}
			}
			else {
				sender.sendMessage(ChatColor.RED+cfg.tr("invalidArgument")+" /rc [test|reload]");
			}
		}
		else {
			success = false;
		}
		
		return success;
	}
	@Override
	public void onDisable() {
		this.out("Disabled");
	}
	@Override
	public void onEnable() {
	    cfg = new KonseptConfig(this);
		loadConfig();
		out("Enabled!  currency: "+currencyString+"   baseCost: "+baseCost);
		babble(cfg.tr("spammy"));
		PluginManager pm =getServer().getPluginManager();
		pm.registerEvents(blockListener,this);
		pm.registerEvents(playerListener,this);
		pm.registerEvents(entityListener,this);
	}
	public boolean permit(Player player,String permission){ 
		return player.hasPermission(permission);
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
	public String plural(int number) {
		if (number == 1){
			return "";
		}
		else {
			return cfg.tr("plural");
		}
	}

	public void loadConfig() {
	    int stringNumber = cfg.refresh();
	    out("Translating "+stringNumber+" strings");
	    triggerString = cfg.get().getString("triggerString","[Repair]");
		verbose = cfg.get().getBoolean("verbose", false);
		currency = cfg.get().getInt("currency",266);
		currencyMaterial = Material.getMaterial(currency);
		if (currencyMaterial == null){
			crap("You have selected an invalid currency ("+currency+"), falling back to GOLD_INGOT!");
			currency = 266;
			currencyMaterial = Material.GOLD_INGOT;
			currencyString = "Gold ingot";
		}
		else if (currencyMaterial.isEdible()){
			crap("You've selected an edible currency.  Due to a bug, this won't work.  Falling back to GOLD_INGOT");
			currency = 266;
			currencyMaterial = Material.GOLD_INGOT;
			currencyString = "Gold ingot";
		}
		else {
			currencyString = cfg.get().getString("currencyString");
		}
		
		baseCost = cfg.get().getDouble("baseCost",0.01);
		currencyName = cfg.get().getString("currencyName","g");
		partialRepair = cfg.get().getBoolean("partialRepair", false);
		distributePartialRepair = cfg.get().getBoolean("distributePartialRepair", true);
	}

}