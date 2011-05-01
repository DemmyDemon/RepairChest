package com.webkonsept.bukkit.repairchest;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;

public class RepairChestBlockListener extends BlockListener {
	private RepairChest plugin;
	private ArrayList<BlockFace> checkFaces = this.getFacesToCheck();
	
	RepairChestBlockListener (RepairChest instance) {
		plugin = instance;
	}
	
	// EVENT HANDLERS //
	
	@Override
	public void onSignChange (SignChangeEvent event){
		if (! plugin.isEnabled()){
			return;
		}
		String tag = event.getLine(0);
		Player player = event.getPlayer();
		if (tag.equalsIgnoreCase("[Repair]")){
			if (event.getBlock().getType().equals(Material.WALL_SIGN)){
				if (plugin.permit(player, "repairchest.create")){
					player.sendMessage("Repair chest authorized!");
				}
				else {
					event.setLine(0, "DENIED!");
					player.sendMessage("Sorry, you lack permission to create repair chests!");
				}
			}
			else {
				event.setLine(0, "Won't work!");
				player.sendMessage("Repair chests only work with wall signs.");
			}
		}
	}
	public void onBlockBreak (BlockBreakEvent event){
		if (! plugin.isEnabled()){
			return;  // If the plugin is disabled, it should not be doing anything.
		}
		if (event.isCancelled()){
			return;  // If it's already canceled, I'm not going to care.
		}
		event.setCancelled( ! this.autorizeRemoval(event.getBlock(), event.getPlayer()));
	}
	
	// SUPPORT METHODS //
	
	private boolean autorizeRemoval (Block block, Player player){
		boolean authorized = true;  // By default, let it be removed.
		if (player != null && plugin.permit(player, "repairchest.destroy")){
			authorized = true;
			/*
			 * Basically "do nothing", but this IF block avoids a lot of work for authorized users.
			 * No need to check block type and blah blah blah if it's just going to be authorized anyway.
			*/
		}
		else if (block.getType().equals(Material.WALL_SIGN)){
			if (this.relevantSign(block)){
				if (player != null){
					player.sendMessage("Permission denied!  You can't remove repair chest signs.");
				}
				authorized = false;
			}
		}
		else {
			Iterator<BlockFace> faces = checkFaces.iterator();
			while (faces.hasNext()){
				BlockFace face = faces.next();
				if (this.relevantSign(block.getFace(face))){
					authorized = false;
					if (player != null){
						player.sendMessage("This block has a protected sign on it, and can't be removed.");
					}
					break;
				}
			}
		}
		return authorized;
	}
	private boolean relevantSign (Block block){
		boolean relevant = false; // Defaults to irrelevance.  Feels good, man!
		if (block.getState() instanceof Sign){
			if (((Sign)block.getState()).getLine(0).equalsIgnoreCase("[Repair]")){
				relevant = true;
			}
		}
		return relevant;
	}
	private ArrayList<BlockFace> getFacesToCheck() {
		ArrayList<BlockFace> checkFaces = new ArrayList<BlockFace>();
		checkFaces.add(BlockFace.NORTH);
		checkFaces.add(BlockFace.SOUTH);
		checkFaces.add(BlockFace.EAST);
		checkFaces.add(BlockFace.WEST);
		checkFaces.add(BlockFace.UP);
		return checkFaces;
	}

}
