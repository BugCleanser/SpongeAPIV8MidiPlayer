package top.dsbbs2.mp;

import java.io.File;
import java.nio.file.Path;

import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.*;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.PermissionService;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import top.dsbbs2.mp.command.PlayMidiCommand;
import top.dsbbs2.mp.command.StopMidiCommand;

@Plugin("midiplayer")
public class MidiPlayer {
	@Inject
	private Game game;
	@Inject
	@ConfigDir(sharedRoot=false)
	private Path configDir;
	@Inject
	private PluginContainer plugin;
	private static MidiPlayer instance;
	{
		instance=this;
	}
	@Listener
	public void regCmd(RegisterCommandEvent<Command.Raw> e)
	{
		getLogger().info("Registering permissions...");
		game.serviceProvider().provide(PermissionService.class).map(i->i.newDescriptionBuilder(plugin)).ifPresent(builder->{
			builder.id("midiplayer.command.play")
					.description(Component.text("Allows the user to execute the playmidi command."))
					.assign(PermissionDescription.ROLE_USER, true)
					.register();
			builder.id("midiplayer.command.stop")
					.description(Component.text("Allows the user to execute the stopmidi command."))
					.assign(PermissionDescription.ROLE_USER, true)
					.register();
		});
		getLogger().info("Registering commands...");
		e.register(plugin,new PlayMidiCommand(),"playmidi");
		e.register(plugin,new StopMidiCommand(),"stopmidi");
	}
	@Listener
	public void init(StartingEngineEvent<Server> e)
	{
		if (!configDir.toFile().isDirectory()) {
			configDir.toFile().mkdirs();
		}
		Lists.newArrayList(MidiPlayer.getInstance().getConfigDir().toFile().listFiles(f->f.isFile()&&f.getName().endsWith(".mid")&&f.getName().contains(" "))).parallelStream().forEach(i->i.renameTo(new File(i.getParent(),i.getName().replace(" ", ""))));
		getLogger().info("MidiPlayer loaded!");
	}
	public PluginContainer getPlugin()
	{
		return this.plugin;
	}
	public Game getGame()
	{
		return this.game;
	}
	public Logger getLogger()
	{
		return this.getPlugin().logger();
	}
	public Path getConfigDir()
	{
		return this.configDir;
	}
	public static MidiPlayer getInstance()
	{
		return instance;
	}
}
