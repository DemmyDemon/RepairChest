package com.webkonsept.bukkit.repairchest.listeners;

import com.webkonsept.bukkit.repairchest.RepairChestPlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.ArrayList;
import java.util.Iterator;

public class RepairChestBlockListener implements Listener {
	private RepairChestPlugin plugin;
	private ArrayList<BlockFace> checkFaces = this.getFacesToCheck();
	
	public RepairChestBlockListener (RepairChestPlugin instance) {
		plugin = instance;
	}
	
	// EVENT HANDLERS //
	
	@EventHandler
	public void onSignChange (final SignChangeEvent event){
		if (! plugin.isEnabled()){
			return;
		}
		String tag = event.getLine(0);
		Player player = event.getPlayer();
		if (tag.equalsIgnoreCase(plugin.triggerString)){
			if (event.getBlock().getType().equals(Material.WALL_SIGN)){
				Block blockBelow = event.getBlock().getRelative(BlockFace.DOWN);
				if (blockBelow.getType().equals(Material.CHEST)){
					if (plugin.permit(player, "repairchest.create")){
						player.sendMessage(plugin.cfg().tr("chestAuthorized"));
						
					}
					else {
						
						event.setLine(0, ""          );
						event.setLine(1, "PERMISSION");
						event.setLine(2, "DENIED"    );
						event.setLine(3, ""          );
						player.sendMessage(plugin.cfg().tr("chestDenied"));
					}
				}
				else {
                    BlockFace face = plugin.chestList.findChest(event.getBlock().getData());
                    Block chestCandidate = event.getBlock().getRelative(face);
                    if (chestCandidate.getType().equals(Material.CHEST)){
                        if (plugin.permit(player, "repairchest.create")){
                            player.sendMessage(plugin.cfg().tr("chestAuthorized"));
                        }
                        else {

                            event.setLine(0, ""          );
                            event.setLine(1, "PERMISSION");
                            event.setLine(2, "DENIED"    );
                            event.setLine(3, ""          );
                            player.sendMessage(plugin.cfg().tr("chestDenied"));
                        }
                    }
                    else  {
					    event.setLine(0,plugin.cfg().tr("chestFirst"));
                    }
				}
			}
			else if (event.getBlock().getType().equals(Material.SIGN_POST)){
				Block signBlock = event.getBlock();
				byte data = signBlock.getData();
				BlockFace chestSide = plugin.chestList.findChest(data);
				Block chestBlock = null;
				if (chestSide != null){
					chestBlock = signBlock.getRelative(chestSide);
				}
				
				if (chestBlock != null && chestBlock.getType().equals(Material.CHEST)){
					byte newSignData = plugin.chestList.signTranslate(data);
					signBlock.setType(Material.WALL_SIGN);
					signBlock.setData(newSignData);
					Sign sign = (Sign)signBlock.getState();
					sign.setLine(0,event.getLine(0));
					sign.setLine(1,event.getLine(1));
					sign.setLine(2,event.getLine(2));
					sign.setLine(3,event.getLine(3));
                    sign.update(true);
				}
				else {
					event.setLine(0,"Won't work!");
					if (chestSide == null){
						event.setLine(1,"Invalid sign");
						event.setLine(2,"angle found!");
					}
					else {
						event.setLine(1,"Chest first!");
					}
				}
			}
			else {
				event.setLine(0, "Won't work!");
				event.setLine(1, "UNSUPPORTED");
				event.setLine(2, "sign type!");
			}
		}
	}
	
	@EventHandler
	public void onBlockBreak (final BlockBreakEvent event){
		if (! plugin.isEnabled()){
			return;  // If the plugin is disabled, it should not be doing anything.
		}
		if (event.isCancelled()){
			return;  // If it's already canceled, I'm not going to care.
		}
		event.setCancelled( ! this.autorizeRemoval(event.getBlock(), event.getPlayer()));
	}
	
	@EventHandler
	public void onBlockBurn (final BlockBurnEvent event){
		if (! plugin.isEnabled()) return;
		if (event.isCancelled()) return;
		event.setCancelled( ! this.autorizeRemoval(event.getBlock(),null)); // Null because this event no player.  Ever.
	}
	
	// SUPPORT METHODS //
	
	public boolean autorizeRemoval (Block block, Player player){
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
					player.sendMessage(plugin.cfg().tr("removePermissionDenied"));
				}
				authorized = false;
			}
		}
		else {
			Iterator<BlockFace> faces = checkFaces.iterator();
			while (faces.hasNext()){
				BlockFace face = faces.next();
				if (this.relevantSign(block.getRelative(face))){
					authorized = false;
					if (player != null){
						player.sendMessage(plugin.cfg().tr("cantRemoveProtected"));
					}
					break;
				}
			}
		}
		return authorized;
	}
	public boolean relevantSign (Block block){
		boolean relevant = false; // Defaults to irrelevance.  Feels good, man!
		if (block.getState() instanceof Sign){
			if (((Sign)block.getState()).getLine(0).equalsIgnoreCase(plugin.triggerString)){
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
