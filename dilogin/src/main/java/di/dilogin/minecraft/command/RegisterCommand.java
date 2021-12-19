package di.dilogin.minecraft.command;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import di.dicore.DIApi;
import di.dilogin.BukkitApplication;
import di.dilogin.controller.DILoginController;
import di.dilogin.controller.LangManager;
import di.dilogin.dao.DIUserDao;
import di.dilogin.dao.DIUserDaoSqlImpl;
import di.dilogin.entity.DIUser;
import di.dilogin.entity.TmpMessage;
import di.dilogin.minecraft.cache.TmpCache;
import di.internal.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Command to register as a user.
 */
public class RegisterCommand implements CommandExecutor {

	/**
	 * User manager in the database.
	 */
	private final DIUserDao userDao = new DIUserDaoSqlImpl();

	/**
	 * Main api.
	 */
	private final DIApi api = BukkitApplication.getDIApi();

	/**
	 * Reactions emoji.
	 */
	private final String emoji = api.getInternalController().getConfigManager().getString("discord_embed_emoji");

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		// We modify this to do something very different
		if(args.length == 2)
		{
			try {
				String minecraftNick = args[0]; 
				long discordId = Long.parseLong(args[1]);
				userDao.add(minecraftNick, discordId);
				Optional<Player> playerOpt = Utils.getUserPlayerByName(api.getInternalController().getPlugin(), minecraftNick);
				if(playerOpt.isPresent()) 
					playerOpt.get().kickPlayer(LangManager.getString(playerOpt.get(), "please_login_again"));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * Send message to user register.
	 * 
	 * @param user         Discord user.
	 * @param player       Bukkit player.
	 * @param messageEmbed Embed message.
	 */
	private void sendMessage(User user, Player player, MessageEmbed messageEmbed) {
		String code = TmpCache.getRegisterMessage(player.getName()).get().getCode();
		user.openPrivateChannel().submit()
				.thenAccept(channel -> channel.sendMessage(messageEmbed).submit().thenAccept(message -> {
					message.addReaction(emoji).queue();
					TmpCache.addRegister(player.getName(), new TmpMessage(player, user, message, code));
				}).whenComplete((message, error) -> {
					if (error == null)
						return;

					TextChannel serverchannel = api.getCoreController().getDiscordApi()
							.getTextChannelById(api.getInternalController().getConfigManager().getLong("channel"));

					serverchannel.sendMessage(user.getAsMention())
						//.delay(Duration.ofSeconds(10))
						//.flatMap(Message::delete)
						.queue();

					Message servermessage = serverchannel.sendMessage(messageEmbed).submit().join();
					servermessage.addReaction(emoji).queue();
					TmpCache.addRegister(player.getName(), new TmpMessage(player, user, servermessage, code));

				}));
	}

	/**
	 * Create the log message according to the configuration.
	 * 
	 * @param player Bukkit player.
	 * @param user   Discord user.
	 * @return Embed message configured.
	 */
	private MessageEmbed getEmbedMessage(Player player, User user) {
		EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(LangManager.getString(player, "register_discord_title"))
				.setDescription(LangManager.getString(user, player, "register_discord_desc")).setColor(
						Utils.hex2Rgb(api.getInternalController().getConfigManager().getString("discord_embed_color")));

		if (api.getInternalController().getConfigManager().getBoolean("discord_embed_server_image")) {
			Optional<Guild> optGuild = Optional.ofNullable(api.getCoreController().getDiscordApi()
					.getGuildById(api.getCoreController().getConfigManager().getLong("discord_server_id")));
			if (optGuild.isPresent()) {
				String url = optGuild.get().getIconUrl();
				if (url != null)
					embedBuilder.setThumbnail(url);
			}
		}

		if (api.getInternalController().getConfigManager().getBoolean("discord_embed_timestamp"))
			embedBuilder.setTimestamp(Instant.now());
		return embedBuilder.build();
	}

	/**
	 * @param string Array of string.
	 * @return Returns a string from array string.
	 */
	private static String arrayToString(String[] string) {
		String respuesta = "";
		for (int i = 0; i < string.length; i++) {
			if (i != string.length - 1) {
				respuesta = String.valueOf(respuesta) + string[i] + " ";
			} else {
				respuesta = String.valueOf(respuesta) + string[i];
			}
		}
		return respuesta;
	}

	/**
	 * Check if the user entered exists.
	 * 
	 * @param name Discord username with discriminator.
	 * @return True if user exists.
	 */
	private static boolean idIsValid(String id) {
		try {
			Long.parseLong(id);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}