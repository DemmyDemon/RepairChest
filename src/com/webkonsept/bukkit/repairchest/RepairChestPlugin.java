package com.webkonsept.bukkit.repairchest;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
    public boolean checkRepairPermission = false;

    Integer[] defaultRepairables = {
        256, 257, 258, 259, 261, 267, 268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 283,
        284, 285, 286, 290, 291, 292, 293, 294, 298, 299, 300, 301, 302, 303, 304, 305, 306, 307, 308,
        309, 310, 311, 312, 313, 314, 315, 316, 317, 346, 359
    };

    private HashSet<Material> repairables = new HashSet<Material>();
	
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
		verbose(cfg.tr("spammy"));
		PluginManager pm =getServer().getPluginManager();
		pm.registerEvents(blockListener,this);
		pm.registerEvents(playerListener,this);
		pm.registerEvents(entityListener,this);
	}
	public boolean permit(Player player,String permission){
        boolean can = player.hasPermission(permission);
        verbose("Permission check for " + player.getName() + " -> " + permission + ": " + can);
		return can;
	}
	public void out(String message) {
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + "] " + message);
	}
	public void error(String message){
		PluginDescriptionFile pdfFile = this.getDescription();
		log.severe("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + " CRAP] " + message);
	}
	public void verbose(String message){
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
        verbose = cfg.get().getBoolean("verbose", false);
	    triggerString = cfg.get().getString("triggerString","[Repair]");
        verbose("Trigger string: " + triggerString);
        checkRepairPermission = cfg().get().getBoolean("checkRepairPermission",false);
        verbose("Check permission before repair: " + checkRepairPermission);

		currency = cfg.get().getInt("currency",266);
		currencyMaterial = Material.getMaterial(currency);
		if (currencyMaterial == null){
			error("You have selected an invalid currency (" + currency + "), falling back to GOLD_INGOT!");
			currency = 266;
			currencyMaterial = Material.GOLD_INGOT;
			currencyString = "Gold ingot";
		}
		else if (currencyMaterial.isEdible()){
			error("You've selected an edible currency.  Due to a bug, this won't work.  Falling back to GOLD_INGOT");
			currency = 266;
			currencyMaterial = Material.GOLD_INGOT;
			currencyString = "Gold ingot";
		}
		else {
			currencyString = cfg.get().getString("currencyString");
		}
        verbose("Currency: " + currencyMaterial.toString());

		baseCost = cfg.get().getDouble("baseCost",0.01);
        verbose("Base cost: " + baseCost);

        currencyName = cfg.get().getString("currencyName","g");
        verbose("Currency name: " + currencyName);

		partialRepair = cfg.get().getBoolean("partialRepair", false);
        verbose("Allow partial repair: " + partialRepair);

		distributePartialRepair = cfg.get().getBoolean("distributePartialRepair", true);
        verbose("Distribute partial repair: " + distributePartialRepair);

        repairables.clear();
        List<Integer> rawRepairable = cfg.get().getIntegerList("repairables");
        if (rawRepairable.size() == 0){
            error("The list of repairables was empty, going with the default set.");
            rawRepairable = Arrays.asList(defaultRepairables);
        }
        for (Integer id : rawRepairable){
            Material mat = Material.getMaterial(id);
            if (mat != null){
                repairables.add(mat);
                verbose("Added " + mat.toString().toLowerCase() + " to the repairable items list");
            }
            else {
                 error("Unknown repairable material ID: " + id);
            }
        }
    }
    public boolean canRepair(ItemStack stack) {
        if (repairables.contains(stack.getType())){
            return true;
        }
        else {
            return false;
        }
    }
}