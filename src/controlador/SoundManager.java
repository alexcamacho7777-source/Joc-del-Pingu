package controlador;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static SoundManager instance;
    private MediaPlayer menuMusic;
    private MediaPlayer gameMusic;
    private MediaPlayer currentMusic;
    private Map<String, AudioClip> sounds = new HashMap<>();
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;

    private SoundManager() {
        loadSounds();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void loadSounds() {
        try {
            // Background Music
            URL menuURL = getClass().getResource("/resources/sounds/menu_music.mp3");
            if (menuURL != null) {
                Media media = new Media(menuURL.toExternalForm());
                menuMusic = new MediaPlayer(media);
                menuMusic.setCycleCount(MediaPlayer.INDEFINITE);
                menuMusic.setVolume(0.5);
            } else {
                System.out.println("No se encontró: /resources/sounds/menu_music.mp3");
            }

            URL gameURL = getClass().getResource("/resources/sounds/game_music.mp3");
            if (gameURL != null) {
                Media media = new Media(gameURL.toExternalForm());
                gameMusic = new MediaPlayer(media);
                gameMusic.setCycleCount(MediaPlayer.INDEFINITE);
                gameMusic.setVolume(0.5);
            } else {
                System.out.println("No se encontró: /resources/sounds/game_music.mp3");
            }

            // Sound Effects
            loadSound("click", "/resources/sounds/click.mp3");
            loadSound("bear", "/resources/sounds/bear.mp3");
            loadSound("hole", "/resources/sounds/hole.mp3");
            loadSound("sled", "/resources/sounds/sled.mp3");
            loadSound("ice", "/resources/sounds/ice.mp3");
            loadSound("event", "/resources/sounds/event.mp3");
            loadSound("win", "/resources/sounds/win.mp3");

        } catch (Exception e) {
            System.err.println("Error cargando sonidos: " + e.getMessage());
        }
    }

    private void loadSound(String name, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                sounds.put(name, new AudioClip(url.toExternalForm()));
            } else {
                System.out.println("No se encontró el sonido: " + path);
            }
        } catch (Exception e) {
            System.err.println("Error cargando " + name + ": " + e.getMessage());
        }
    }

    public void playSound(String name) {
        if (soundEnabled && sounds.containsKey(name)) {
            sounds.get(name).play();
        }
    }

    public void playSoundOnce(String name) {
        if (soundEnabled && sounds.containsKey(name)) {
            AudioClip clip = sounds.get(name);
            if (!clip.isPlaying()) {
                clip.play();
            }
        }
    }

    public void stopSound(String name) {
        if (sounds.containsKey(name)) {
            sounds.get(name).stop();
        }
    }

    public void playMenuMusic() {
        if (!musicEnabled || menuMusic == null) return;
        if (currentMusic == menuMusic) return; // Ya está sonando
        
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic = menuMusic;
        currentMusic.play();
    }

    public void playGameMusic() {
        if (!musicEnabled || gameMusic == null) return;
        if (currentMusic == gameMusic) return; // Ya está sonando
        
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic = gameMusic;
        currentMusic.play();
    }

    public void stopBackgroundMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }

    public void setMusicVolume(double volume) {
        if (menuMusic != null) menuMusic.setVolume(volume);
        if (gameMusic != null) gameMusic.setVolume(volume);
    }

    public void setSoundVolume(double volume) {
        for (AudioClip clip : sounds.values()) {
            clip.setVolume(volume);
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (enabled) {
            if (currentMusic != null) currentMusic.play();
        } else {
            if (currentMusic != null) currentMusic.pause();
        }
    }

    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isMusicEnabled() { return musicEnabled; }
}
