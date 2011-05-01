package com.webkonsept.bukkit.repairchest;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

public class RepairChestBlockListener extends BlockListener {
	private RepairChest plugin;
	
	RepairChestBlockListener (RepairChest instance) {
		plugin = instance;
	}
	
	@Override
	public void onSignChange (SignChangeEvent event){
		String tag = event.getLine(0);
		Player player = event.getPlayer();
		if (tag.equalsIgnoreCase("[Repair]")){
			if (plugin.permit(player, "repairchest.create")){
				player.sendMessage("Repair chest authorized!");
			}
			else {
				event.setLine(0, "DENIED!");
				player.sendMessage("Sorry, you lack permission to create repair chests!");
			}
		}
	}
	public void onBlockBreak (BlockBreakEvent event){
		Material blockType = event.getBlock().getType();
		if (blockType.equals(Material.SIGN_POST) || blockType.equals(Material.WALL_SIGN)){
			Sign sign = (Sign) event.getBlock().getState();
			Player player = event.getPlayer();
			if (sign.getLine(0).equalsIgnoreCase("[Repair]")){
				if (plugin.permit(player, "repairchest.destroy")){
					player.sendMessage("Repair chest removed!");
				}
				else {
					event.setCancelled(true);
					player.sendMessage("Permission denied!  You can't remove repair chests.");
				}
			}
		}
	}

}
