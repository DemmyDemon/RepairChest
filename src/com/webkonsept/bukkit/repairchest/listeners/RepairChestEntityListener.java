package com.webkonsept.bukkit.repairchest.listeners;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.webkonsept.bukkit.repairchest.RepairChestPlugin;

public class RepairChestEntityListener implements Listener {
	private RepairChestPlugin plugin;
	

	public RepairChestEntityListener (RepairChestPlugin instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onEntityExplode (final EntityExplodeEvent event){
		if (!plugin.isEnabled()) return;
		if (event.isCancelled()) return;
		
		for (Block block : event.blockList()){
		    if (plugin.blockListener.relevantSign(block)){ // Have no Player to offer here...
				event.setCancelled(true);
				plugin.getServer().broadcastMessage(ChatColor.DARK_AQUA+plugin.cfg().tr("explosionBroadcast"));
				break;
			}
		}
	}
}
