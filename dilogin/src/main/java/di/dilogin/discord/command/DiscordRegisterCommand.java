package di.dilogin.discord.command;

import java.time.Duration;
import java.util.Optional;

import org.bukkit.entity.Player;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.DILoginController;
import di.dilogin.controller.LangManager;
import di.dilogin.dao.DIUserDao;
import di.dilogin.dao.DIUserDaoSqlImpl;
import di.dilogin.entity.AuthmeHook;
import di.dilogin.entity.CodeGenerator;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import di.dilogin.minecraft.event.UserLoginEventImpl;
import di.dilogin.minecraft.util.Util;
import di.internal.entity.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Command to register as a user.
 */
public class DiscordRegisterCommand implements DiscordCommand {

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = new DIUserDaoSqlImpl();

	/**
	 * Main api.
	 */
	private final DIApi api = BukkitApplication.getDIApi();

	@Override
	public void execute(String message, MessageReceivedEvent event) {

		//event.getChannel().sendMessage("`public void execute: message = " +message.toString() + "`").queue();
		
		//event.getMessage().delete().delay(Duration.ofSeconds(20)).queue();
		if (userDao.containsDiscordId(event.getAuthor().getIdLong())) {
			event.getChannel().sendMessage(LangManager.getString("register_already_exists").replace("%existing_name%", userDao.getUserForId(event.getAuthor().getIdLong())))
					//.delay(Duration.ofSeconds(20))
					//.flatMap(Message::delete)
					.queue();
			return;
		}

		// Check account limits.
		if (userDao.getDiscordUserAccounts(event.getAuthor()) >= api.getInternalController().getConfigManager()
				.getInt("register_max_discord_accounts")) {
			event.getChannel().sendMessage(LangManager.getString("register_max_accounts"))
					//.delay(Duration.ofSeconds(20))
					//.flatMap(Message::delete)
					.queue();
			return;
		}

		// Check arguments.
		if (message.equals("") || message.isEmpty()) {
			event.getChannel().sendMessage(LangManager.getString("register_discord_arguments"))
					//.delay(Duration.ofSeconds(10))
					//.flatMap(Message::delete)
					.queue();
			return;
		}

		// Check code.
		Optional<TmpMessage> tmpMessageOpt = TmpCache.getRegisterMessageByCode(message);
		if (!tmpMessageOpt.isPresent()) {
			event.getChannel().sendMessage(LangManager.getString("register_code_not_found"))
					//.delay(Duration.ofSeconds(10))
					//.flatMap(Message::delete)
					.queue();
			return;
		}

		Player player = tmpMessageOpt.get().getPlayer();
		// Create password.
		String password = CodeGenerator.getCode(8, api);
		player.sendMessage(LangManager.getString(event.getAuthor(), player, "register_success")
				.replace("%authme_password%", password));
		
		// Send message to discord.
		MessageEmbed messageEmbed = getEmbedMessage(player, event.getAuthor());
		event.getChannel().sendMessage(messageEmbed)
			//.delay(Duration.ofSeconds(10))
			//.flatMap(Message::delete)
			.queue();
		
		// Remove user from register cache.
		TmpCache.removeRegister(player.getName());
		// Add user to data base.
		userDao.add(new DIUser(Optional.of(player), event.getAuthor()));

		if (DILoginController.isAuthmeEnabled()) {
			AuthmeHook.register(player, password);
		} else {
			
			Optional<DIUser> userOpt = UserLoginEventImpl.userDao.get(player.getName());
			if(userOpt.isPresent())
			{
				if (!Util.isWhiteListed(userOpt.get().getPlayerDiscord())) {
					di.dilogin.controller.DILoginController.kickPlayer(player, LangManager.getString(player, "login_without_role_required"));
				} else DILoginController.loginUser(player, event.getAuthor());
			}
		}

	}

	@Override
	public String getAlias() {
		return api.getInternalController().getConfigManager().getString("register_command");
	}

	/**
	 * Create the log message according to the configuration.
	 * 
	 * @param player Bukkit player.
	 * @param user   Discord user.
	 * @return Embed message configured.
	 */
	private MessageEmbed getEmbedMessage(Player player, User user) {
		EmbedBuilder embedBuilder = DILoginController.getEmbedBase()
				.setTitle(LangManager.getString(player, "register_discord_title"))
				.setDescription(LangManager.getString(user, player, "register_discord_success"));
		return embedBuilder.build();
	}

}
