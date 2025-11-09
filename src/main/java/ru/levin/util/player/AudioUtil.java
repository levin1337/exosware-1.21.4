package ru.levin.util.player;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.Manager;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

public class AudioUtil implements IMinecraft {

    public static void playSound(String name) {

        Identifier id = Identifier.of("exosware", "sounds/" + name);
        mc.execute(() -> {
            try {
                Optional<Resource> resourceOpt = mc.getResourceManager().getResource(id);
                if (resourceOpt.isEmpty()) {
                    System.err.println("Sound resource not found: " + id);
                    return;
                }

                Resource resource = resourceOpt.get();
                try (InputStream in = resource.getInputStream()) {
                    byte[] soundData = in.readAllBytes();
                    float volume = (float) (Manager.FUNCTION_MANAGER.clientSounds.volume.get().intValue() / 100f);

                    new Thread(() -> playSoundFromBytes(soundData, volume)).start();
                }
            } catch (Exception e) {
                System.err.println("Error loading sound: " + e.getMessage());
            }
        });
    }

    private static void playSoundFromBytes(byte[] soundData, float volume) {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(soundData))) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (20 * Math.log10(volume));
            gainControl.setValue(Math.min(Math.max(dB, gainControl.getMinimum()), gainControl.getMaximum()));

            clip.start();

            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (Exception e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }


}