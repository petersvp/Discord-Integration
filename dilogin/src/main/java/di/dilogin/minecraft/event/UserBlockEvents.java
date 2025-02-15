package di.dilogin.minecraft.event;

import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.DILoginController;
import di.dilogin.controller.LangManager;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.cache.UserBlockedCache;
import di.dilogin.minecraft.util.Util;
import net.dv8tion.jda.api.entities.User;

@SuppressWarnings("deprecation")
public class UserBlockEvents implements Listener {
	
	private final DIApi api = BukkitApplication.getDIApi();
	
	private void SendReplyResponse(Player player)
	{
		//player.sendMessage(LangManager.getString(player, "login_without_role_required"))
		BukkitApplication.getDIApi();
		Optional<TmpMessage> pendingRegistration = TmpCache.getRegisterMessage(player.getName());
		if (pendingRegistration.isPresent()){
			String code = pendingRegistration.get().getCode();
			String regcommand = api.getCoreController().getBot().getPrefix() + api.getInternalController().getConfigManager().getString("register_command") + " " + code;
			player.sendMessage(LangManager.getString(player, "register_arguments")
					.replace("%register_command%", regcommand));
		}
		else {
			
			Optional<DIUser> userOpt = UserLoginEventImpl.userDao.get(player.getName());
			if(userOpt.isPresent())
			{
				if (!Util.isWhiteListed(userOpt.get().getPlayerDiscord())) {
					player.sendMessage(LangManager.getString(player, "login_without_role_required"));
					di.dilogin.controller.DILoginController.kickPlayer(player, LangManager.getString(player, "login_without_role_required"));
				}
				else
					player.sendMessage(LangManager.getString(player, "login_request"));
			}	
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerSendCommand(PlayerCommandPreprocessEvent event) {
//		String message = event.getMessage();
//		if (message.indexOf(" ") == -1) {
//			if (message.equalsIgnoreCase("/register") || message.equalsIgnoreCase("/login"))
//				return;
//		} else if (message.split(" ")[0].equalsIgnoreCase("/register") || message.split(" ")[0].equalsIgnoreCase("/login") ) {
//			return;
//		}
		if (UserBlockedCache.contains(event.getPlayer().getName()))
		{
			event.setCancelled(true);
			Player player = event.getPlayer();
			SendReplyResponse(player);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
		{
			String enteredCode = event.getMessage();
			Optional<TmpMessage> pendingLogin = TmpCache.getLoginMessage(event.getPlayer().getName());
			if(pendingLogin.isPresent() && pendingLogin.get().getCode().equalsIgnoreCase(enteredCode)) {
				DILoginController.loginUser(event.getPlayer(), pendingLogin.get().getUser());
				event.setCancelled(true);
				return;
			}
			event.setCancelled(true);
			Player player = event.getPlayer();
			SendReplyResponse(player);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMove(PlayerMoveEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()) && (event.getFrom().getX() != event.getTo().getX()
				|| event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() < event.getTo().getZ())) {
			Location loc = event.getFrom();
			event.getPlayer().teleport(loc.setDirection(event.getTo().getDirection()));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHungerDecrease(FoodLevelChangeEvent event) {
		if (event.getEntity().getType() != EntityType.PLAYER)
			return;
		Player player = (Player) event.getEntity();
		if (UserBlockedCache.contains(player.getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onOpenInventory(InventoryOpenEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent event) {
		if (UserBlockedCache.contains(event.getWhoClicked().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onUserDejaCaerItem(PlayerDropItemEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onUserCambiaDeModo(PlayerGameModeChangeEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInteractEvent(PlayerInteractEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onUserCambiaObjetoDeMano(PlayerItemHeldEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onUserLevantaObjeto(PlayerPickupItemEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onUserPortal(PlayerPortalEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onConsumeItem(PlayerItemConsumeEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemDamage(PlayerItemDamageEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockDamageEvent(BlockDamageEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlacedEvent(BlockPlaceEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlacedEvent(SignChangeEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockDamage(EntityDamageByBlockEvent event) {
		if (event.getEntityType() != EntityType.PLAYER)
			return;
		Player player = (Player) event.getEntity();
		if (UserBlockedCache.contains(player.getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntityType() != EntityType.PLAYER)
			return;
		Player player = (Player) event.getEntity();
		if (UserBlockedCache.contains(player.getName()))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity().getType() == EntityType.PLAYER) {
			Player player = (Player) event.getEntity();
			if (UserBlockedCache.contains(player.getName()))
				event.setCancelled(true);
		} else if (event.getDamager().getType() == EntityType.PLAYER) {
			Player player = (Player) event.getDamager();
			if (UserBlockedCache.contains(player.getName()))
				event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAnimation(PlayerAnimationEvent event) {
		if (UserBlockedCache.contains(event.getPlayer().getName()))
			event.setCancelled(true);
	}
}
