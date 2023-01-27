package top.dsbbs2.mp.command;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.*;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.entity.living.player.User;

import com.google.common.collect.Lists;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import top.dsbbs2.mp.MidiPlayer;
import top.dsbbs2.mp.util.MidiUtil;

public class PlayMidiCommand implements Command.Raw {

	@Override
	public CommandResult process(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {
		if (arguments.input().isEmpty()) {
			throw new CommandException(Component.text("Argument midi_file is required"));
		}
		if(!new File(MidiPlayer.getInstance().getConfigDir().toFile(),arguments.input()).isFile())
			throw new CommandException(Component.text("midi_file does not exist"));
		if(cause.first(ServerPlayer.class).isPresent())
		{
			User p=cause.first(ServerPlayer.class).get().user();
			try {
				MidiUtil.stop(p.uniqueId());
				MidiUtil.playMidi(new FileInputStream(new File(MidiPlayer.getInstance().getConfigDir().toFile(),arguments.input())),1.0f,p.uniqueId(),MidiUtil.PlaySoundAble.newPlaySoundAble((User) p));
			} catch (Throwable e) {
				throw new CommandException(Component.text("Error occurred"), e);
			}
		}else {
			throw new CommandException(Component.text("This command can only be executed by a player"));
		}
		return CommandResult.success();
	}

	@Override
	public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {
		Lists.newArrayList(MidiPlayer.getInstance().getConfigDir().toFile().listFiles(f->f.isFile()&&f.getName().endsWith(".mid")&&f.getName().contains(" "))).parallelStream().forEach(i->i.renameTo(new File(i.getParent(),i.getName().replace(" ", ""))));
		return Lists.newArrayList(MidiPlayer.getInstance().getConfigDir().toFile().listFiles(f->f.isFile()&&f.getName().endsWith(".mid")&&!f.getName().contains(" "))).parallelStream().map(File::getName).filter(i->i.toLowerCase(Locale.getDefault()).startsWith(arguments.input().toLowerCase(Locale.getDefault()))).map(CommandCompletion::of).collect(Collectors.toList());
	}

	@Override
	public boolean canExecute(CommandCause cause) {
		return cause.subject().hasPermission("midiplayer.command.play");
	}

	@Override
	public Optional<Component> shortDescription(CommandCause cause) {
		return Optional.of(Component.text("Play Midi"));
	}

	@Override
	public Optional<Component> extendedDescription(CommandCause cause) {
		return shortDescription(cause);
	}

	@Override
	public Optional<Component> help(CommandCause var1) {
		return Optional.of(Component.text("/playmidi <midi_file>"));
	}

	@Override
	public Component usage(CommandCause cause) {
		return help(cause).orElse(null);
	}
}
