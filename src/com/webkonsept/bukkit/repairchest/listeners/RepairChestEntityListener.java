package com.webkonsept.bukkit.repairchest.listeners;

import java.util.Iterator;
import java.util.List;

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
		List<Block> blocks = event.blockList();
		Iterator<Block> iterator = blocks.iterator();
		boolean cancelEvent = false;
		while (iterator.hasNext()){
			Block block = iterator.next();
			if (plugin.blockListener.relevantSign(block)){ // Have no Player to offer here...
				cancelEvent = true;
				plugin.getServer().broadcastMessage(ChatColor.DARK_AQUA+"Everyone, please refrain from blowing up repair chests.  It's not nice.");
				break;
			}
		}
		event.setCancelled(cancelEvent);
	}
}
