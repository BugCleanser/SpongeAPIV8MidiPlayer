package top.dsbbs2.mp.util;

import javax.sound.midi.*;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.adventure.SpongeAdventure;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
public class MidiUtil
{
    private static final byte[] instruments;
    private static ConcurrentHashMap<Object, Sequencer> playing;
    
    static {
        instruments = new byte[] { 0, 0, 0, 0, 0, 0, 0, 11, 6, 6, 6, 6, 9, 9, 15, 11, 10, 5, 5, 10, 10, 10, 10, 10, 5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 1, 1, 1, 1, 1, 1, 0, 10, 10, 1, 0, 0, 0, 4, 0, 0, 0, 0, 8, 8, 8, 12, 8, 14, 14, 14, 14, 14, 8, 8, 8, 8, 8, 14, 14, 8, 8, 8, 8, 8, 8, 8, 14, 8, 8, 8, 8, 14, 8, 8, 5, 8, 12, 1, 1, 0, 0, 8, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 0, 12, 11, 11, 3, 3, 3, 14, 10, 6, 6, 3, 3, 2, 2, 2, 6, 5, 1, 1, 1, 13, 13, 2, 4, 7 };
        MidiUtil.playing = new ConcurrentHashMap<Object, Sequencer>();
    }
    
    private static byte byteInstrument(final int patch) {
        if (patch < 0 || patch >= MidiUtil.instruments.length) {
            return 0;
        }
        return MidiUtil.instruments[patch];
    }
    
    public static String patchToInstrument(final int patch) {
        return Instrument.fromByte(byteInstrument(patch)).getSound();
    }
    
    public static void stop(final Object id) {
        if (MidiUtil.playing.containsKey(id)) {
            final Sequencer sequencer = MidiUtil.playing.get(id);
            try {
                sequencer.getReceiver().close();
            }
            catch (MidiUnavailableException ex) {}
            sequencer.stop();
            sequencer.close();
            MidiUtil.playing.remove(id);
        }
    }
    
    public static SoundType soundAttempt(final String attempt, final String fallback) {
        SoundType sound = null;
        try {
            sound = (SoundType)SoundTypes.class.getField(attempt).get(null);
            if (sound==null) {
				throw new NoSuchElementException();
			}
        }
        catch (Exception e) {
            try {
                sound = (SoundType)SoundTypes.class.getField(fallback).get(null);
            }
            catch (Exception ex) {}
        }
        /*if (sound == null) {
            sound = SoundTypes.ENTITY_PLAYER_LEVELUP;
        }*/
        return sound;
    }
    
    public static void playMidi(final Sequence sequence, final float tempo, final Object ID, final PlaySoundAble... listeners) {
        try {
            final Sequencer sequencer = MidiSystem.getSequencer(false);
            sequencer.setSequence(sequence);
            sequencer.open();
            sequencer.setTempoFactor(tempo);
            final NoteBlockReceiver reciever = new NoteBlockReceiver(ID, listeners);
            sequencer.getTransmitter().setReceiver(reciever);
            sequencer.start();
            MidiUtil.playing.put(ID, sequencer);
        }
        catch (InvalidMidiDataException | MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void playMidi(final InputStream stream, final float tempo, final Object ID, final PlaySoundAble... listeners) {
    	//Task.builder().execute(() -> {
			try {
				playMidi(MidiSystem.getSequence(stream), tempo, ID, listeners);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		//}).submit(MidiPlayer.getInstance().getPlugin());
    }
    
    public static void stopAll() {
        MidiUtil.playing.forEach((id, s) -> s.stop());
    }
    
    public static boolean isPlaying(final Object key) {
        return MidiUtil.playing.containsKey(key);
    }
    
    public enum Instrument
    {
        PIANO("PIANO", 0, 0, "minecraft:block.note_block.harp"),
        BASS("BASS", 1, 1, "minecraft:block.note_block.bass"),
        SNARE_DRUM("SNARE_DRUM", 2, 2, "minecraft:block.note_block.snare"),
        STICKS("STICKS", 3, 3, "minecraft:block.note_block.hat"),
        BASE_DRUM("BASE_DRUM", 4, 4, "minecraft:block.note_block.basedrum"),
        GUITAR("GUITAR", 5, 5, "minecraft:block.note_block.guitar"),
        BELL("BELL", 6, 6, "minecraft:block.note_block.bell"),
        CHIME("CHIME", 7, 7, "minecraft:block.note_block.chime"),
        FLUTE("FLUTE", 8, 8, "minecraft:block.note_block.flute"),
        XYLOPHONE("XYLOPHONE", 9, 9, "minecraft:block.note_block.xylophone"),
        PLING("PLING", 10, 10, "minecraft:block.note_block.pling"),
        BANJO("BANJO", 11, 11, "minecraft:block.note_block.banjo"),
        BIT("BIT", 12, 12, "minecraft:block.note_block.bit"),
        COW_BELL("COW_BELL", 13, 13, "minecraft:block.note_block.cow_bell"),
        DIDGERIDOO("DIDGERIDOO", 14, 14, "minecraft:block.note_block.didgeridoo"),
        IRON_XYLOPHONE("IRON_XYLOPHONE", 15, 15, "minecraft:block.note_block.iron_xylophone");
        
        private final int pitch;
        private String sound;
        
        private Instrument(final String s, final int n, final int pitch, final String key) {
            this.sound = key;
            this.pitch = pitch;
        }
        
        public static Instrument fromByte(final byte instrument) {
            switch (instrument) {
                case 1: {
                    return Instrument.BASS;
                }
                case 2: {
                    return Instrument.SNARE_DRUM;
                }
                case 3: {
                    return Instrument.STICKS;
                }
                case 4: {
                    return Instrument.BASE_DRUM;
                }
                case 5: {
                    return Instrument.GUITAR;
                }
                case 6: {
                    return Instrument.BELL;
                }
                case 7: {
                    return Instrument.CHIME;
                }
                case 8: {
                    return Instrument.FLUTE;
                }
                case 9: {
                    return Instrument.XYLOPHONE;
                }
                case 10: {
                    return Instrument.PLING;
                }
                case 11: {
                    return Instrument.BANJO;
                }
                case 12: {
                    return Instrument.BIT;
                }
                case 13: {
                    return Instrument.COW_BELL;
                }
                case 14: {
                    return Instrument.DIDGERIDOO;
                }
                case 15: {
                    return Instrument.IRON_XYLOPHONE;
                }
                default: {
                    return Instrument.PIANO;
                }
            }
        }
        
        public String getSound() {
            return this.sound;
        }
        
        public byte getByte() {
            return (byte)this.pitch;
        }
    }
    
    public interface PlaySoundAble
    {
        void playSound(final String p0, final float p1, final float p2);
        
        static PlaySoundAble newPlaySoundAble(final User player) {
            return new PlaySoundAble() {
                @Override
                public void playSound(final String var1, final float var2, final float var3) {
                    if (player.isOnline()) {
                        //try {
                            //player.player().ifPresent(i -> ((ServerGamePacketListener) (i.connection())).getConnection().send(new ClientboundSoundEntityPacket(new SoundEvent(new ResourceLocation(var1)), SoundSource.MASTER, (Entity) i, var2, var3)));
                            player.player().ifPresent(i -> ((ServerGamePacketListener) (i.connection())).getConnection().send(new ClientboundCustomSoundPacket(new ResourceLocation(var1),SoundSource.MASTER,new Vec3(i.position().x(),i.position().y(),i.position().z()),var2,var3)));
                        //System.out.println(true);
                        //}catch (Throwable t){t.printStackTrace();}
                    }
                }
            };
        }
        
        static PlaySoundAble newPlaySoundAble(final Location loc) {
            return new PlaySoundAble() {
                @Override
                public void playSound(final String var1, final float var2, final float var3) {
                    loc.world().playSound(Sound.sound(Key.key(var1), Sound.Source.MASTER,var2,var3),loc.position());
                }
            };
        }
    }
    
    public static class NoteBlockReceiver implements Receiver
    {
        private final Map<Integer, Integer> patches;
        private final PlaySoundAble[] listeners;
        private final Object ID;
        
        public NoteBlockReceiver(final Object ID, final PlaySoundAble... listeners) {
            this.patches = new HashMap<Integer, Integer>();
            this.listeners = listeners;
            this.ID = ID;
        }
        
        @Override
        public void send(final MidiMessage midi, final long time) {
            if (midi instanceof ShortMessage) {
                final ShortMessage message = (ShortMessage)midi;
                final int channel = message.getChannel();
                switch (message.getCommand()) {
                    case 192: {
                        final int patch = message.getData1();
                        this.patches.put(channel, patch);
                        break;
                    }
                    case 144: {
                        final float volume = 10.0f * (message.getData2() / 127.0f);
                        final float pitch = this.getNote(this.toMCNote(message.getData1()));
                        String instrument = Instrument.PIANO.getSound();
                        final Optional<Integer> optional = Optional.ofNullable(this.patches.get(message.getChannel()));
                        if (optional.isPresent()) {
                            instrument = MidiUtil.patchToInstrument(optional.get());
                        }
                        PlaySoundAble[] listeners;
                        for (int length = (listeners = this.listeners).length, i = 0; i < length; ++i) {
                            final PlaySoundAble player = listeners[i];
                            player.playSound(instrument, volume, pitch);
                        }
                        break;
                    }
                }
            }
        }
        
        public float getNote(final byte note) {
            return (float)Math.pow(2.0, (note - 12) / 12.0);
        }
        
        private byte toMCNote(final int n) {
            if (n < 54) {
                return (byte)((n - 6) % 12);
            }
            if (n > 78) {
                return (byte)((n - 6) % 12 + 12);
            }
            return (byte)(n - 54);
        }
        
        @Override
        public void close() {
            MidiUtil.stop(this.ID);
            this.patches.clear();
        }
    }
}
