package top.dsbbs2.mp.command;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.entity.living.player.Player;
import top.dsbbs2.mp.util.MidiUtil;

import java.util.List;
import java.util.Optional;

public class StopMidiCommand implements Command.Raw {

	@Override
	public CommandResult process(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {
		if(cause.first(Player.class).isPresent())
		{
			MidiUtil.stop(cause.first(Player.class).get().uniqueId());
		}else {
			throw new CommandException(Component.text("This command can only be executed by a player"));
		}
		return CommandResult.success();
	}

	@Override
	public List<CommandCompletion> complete(CommandCause cause, ArgumentReader.Mutable arguments) throws CommandException {
		return Lists.newArrayList();
	}

	@Override
	public boolean canExecute(CommandCause cause) {
		return cause.subject().hasPermission("midiplayer.command.stop");
	}

	@Override
	public Optional<Component> shortDescription(CommandCause cause) {
		return Optional.of(Component.text("Stop playing midi"));
	}

	@Override
	public Optional<Component> extendedDescription(CommandCause cause) {
		return shortDescription(cause);
	}

	@Override
	public Optional<Component> help(CommandCause var1) {
		return Optional.of(Component.text("/stopmidi"));
	}

	@Override
	public Component usage(CommandCause cause) {
		return help(cause).orElse(null);
	}
}
