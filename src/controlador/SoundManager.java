package controlador;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * GESTOR DE SO DE L'APLICACIÓ (SINGLETON).
 * CONTROLA LA REPRODUCCIÓ DE MÚSICA DE FONS I ELS EFECTES DE SO DELS ESDEVENIMENTS.
 */
public class SoundManager {
    private static SoundManager instance;
    private MediaPlayer menuMusic;
    private MediaPlayer gameMusic;
    private MediaPlayer currentMusic;
    private Map<String, AudioClip> sounds = new HashMap<>();
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    private double musicVolume = 0.5;
    private double soundVolume = 1.0;

    /**
     * CONSTRUCTOR PRIVAT QUE CARREGA TOTS ELS RECURSOS D'ÀUDIO.
     */
    private SoundManager() {
        loadSounds();
    }

    /**
     * RETORNA L'INSTÀNCIA ÚNICA DEL GESTOR DE SO.
     */
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * CARREGA ELS FITXERS MP3 DES DELS RECURSOS DEL PROJECTE.
     */
    private void loadSounds() {
        try {
            // MÚSICA DE FONS PER ALS MENÚS
            URL menuURL = getClass().getResource("/resources/sounds/menu_music.mp3");
            if (menuURL != null) {
                Media media = new Media(menuURL.toExternalForm());
                menuMusic = new MediaPlayer(media);
                menuMusic.setCycleCount(MediaPlayer.INDEFINITE);
                menuMusic.setVolume(0.5);
            }

            // MÚSICA DE FONS PER A LA PARTIDA
            URL gameURL = getClass().getResource("/resources/sounds/game_music.mp3");
            if (gameURL != null) {
                Media media = new Media(gameURL.toExternalForm());
                gameMusic = new MediaPlayer(media);
                gameMusic.setCycleCount(MediaPlayer.INDEFINITE);
                gameMusic.setVolume(0.5);
            }

            // EFECTES DE SO ESPECÍFICS
            loadSound("click", "/resources/sounds/click.mp3");
            loadSound("bear", "/resources/sounds/bear.mp3");
            loadSound("hole", "/resources/sounds/hole.mp3");
            loadSound("sled", "/resources/sounds/sled.mp3");
            loadSound("ice", "/resources/sounds/ice.mp3");
            loadSound("event", "/resources/sounds/event.mp3");
            loadSound("win", "/resources/sounds/win.mp3");

        } catch (Exception e) {
            System.err.println("ERROR CARREGANT ELS SONS DEL JOC.");
        }
    }

    private void loadSound(String name, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                sounds.put(name, new AudioClip(url.toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("ERROR CARREGANT L'EFECTE: " + name);
        }
    }

    /**
     * REPRODUEIX UN EFECTE DE SO PEL SEU NOM.
     */
    public void playSound(String name) {
        if (soundEnabled && sounds.containsKey(name)) {
            sounds.get(name).play();
        }
    }

    /**
     * REPRODUEIX UN EFECTE NOMÉS SI NO ESTÀ SONANT ACTUALMENT.
     */
    public void playSoundOnce(String name) {
        if (soundEnabled && sounds.containsKey(name)) {
            AudioClip clip = sounds.get(name);
            if (!clip.isPlaying()) {
                clip.play();
            }
        }
    }

    /**
     * ATURA LA REPRODUCCIÓ D'UN EFECTE DE SO.
     */
    public void stopSound(String name) {
        if (sounds.containsKey(name)) {
            sounds.get(name).stop();
        }
    }

    /**
     * INICIA LA MÚSICA DELS MENÚS.
     */
    public void playMenuMusic() {
        if (musicEnabled && menuMusic != null) {
            if (currentMusic != menuMusic) {
                if (currentMusic != null) {
                    currentMusic.stop();
                }
                currentMusic = menuMusic;
                currentMusic.play();
            }
        }
    }

    /**
     * INICIA LA MÚSICA DE L'ENTORN DE JOC.
     */
    public void playGameMusic() {
        if (musicEnabled && gameMusic != null) {
            if (currentMusic != gameMusic) {
                if (currentMusic != null) {
                    currentMusic.stop();
                }
                currentMusic = gameMusic;
                currentMusic.play();
            }
        }
    }

    /**
     * ATURA QUALSEVOL MÚSICA DE FONS QUE ESTIGUI SONANT.
     */
    public void stopBackgroundMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }

    /**
     * AJUSTA EL VOLUM DE LA MÚSICA DE FONS.
     */
    public void setMusicVolume(double volume) {
        this.musicVolume = volume;
        if (menuMusic != null) {
            menuMusic.setVolume(volume);
        }
        if (gameMusic != null) {
            gameMusic.setVolume(volume);
        }
    }

    /**
     * AJUSTA EL VOLUM DE TOTS ELS EFECTES DE SO.
     */
    public void setSoundVolume(double volume) {
        this.soundVolume = volume;
        for (AudioClip clip : sounds.values()) {
            clip.setVolume(volume);
        }
    }

    /**
     * ACTIVA O DESACTIVA ELS EFECTES DE SO.
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    /**
     * ACTIVA O DESACTIVA LA MÚSICA DE FONS.
     */
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (enabled) {
            if (currentMusic != null) {
                currentMusic.play();
            }
        } else {
            if (currentMusic != null) {
                currentMusic.pause();
            }
        }
    }

    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isMusicEnabled() { return musicEnabled; }
    public double getMusicVolume() { return musicVolume; }
    public double getSoundVolume() { return soundVolume; }
}
