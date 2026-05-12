package controlador;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * GESTOR DE SO DE L'APLICACIÓ (SINGLETON).
 * Aquesta classe centralitza tota la gestió d'àudio del projecte. Implementa el 
 * patró Singleton per assegurar que només hi hagi un controlador de so actiu, 
 * evitant conflictes en la reproducció i permetent un control unificat del 
 * volum i els estats (Activat/Desactivat).
 * 
 * @author Alex Camacho
 * @version 1.2
 */
public class SoundManager {
    
    // Instància única del Singleton
    private static SoundManager instance;
    
    // Reproductors per a les diferents músiques de fons (loops)
    private MediaPlayer menuMusic;
    private MediaPlayer gameMusic;
    private MediaPlayer currentMusic;
    
    // Diccionari per emmagatzemar els efectes de so curts (clips)
    private Map<String, AudioClip> sounds = new HashMap<>();
    
    // Estats de configuració
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    private double musicVolume = 0.5;
    private double soundVolume = 1.0;

    /**
     * CONSTRUCTOR PRIVAT.
     * Es crida només un cop des de getInstance(). Carrega tots els fitxers de so.
     */
    private SoundManager() {
        loadSounds();
    }

    /**
     * RETORNA L'INSTÀNCIA ÚNICA DEL GESTOR DE SO.
     * Si no existeix, la crea.
     */
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * CARREGA ELS FITXERS MP3 DES DELS RECURSOS DEL PROJECTE.
     * Inicialitza els reproductors de música i el mapa d'efectes.
     */
    private void loadSounds() {
        try {
            // 1. CONFIGURACIÓ DE LA MÚSICA DEL MENÚ
            URL menuURL = getClass().getResource("/resources/sounds/menu_music.mp3");
            if (menuURL != null) {
                Media media = new Media(menuURL.toExternalForm());
                menuMusic = new MediaPlayer(media);
                menuMusic.setCycleCount(MediaPlayer.INDEFINITE); // Bucle infinit
                menuMusic.setVolume(musicVolume);
            }

            // 2. CONFIGURACIÓ DE LA MÚSICA DE JOC (In-Game)
            URL gameURL = getClass().getResource("/resources/sounds/game_music.mp3");
            if (gameURL != null) {
                Media media = new Media(gameURL.toExternalForm());
                gameMusic = new MediaPlayer(media);
                gameMusic.setCycleCount(MediaPlayer.INDEFINITE);
                gameMusic.setVolume(musicVolume);
            }

            // 3. CÀRREGA DELS EFECTES DE SO (AudioClips per a latència baixa)
            loadSound("click", "/resources/sounds/click.mp3");
            loadSound("bear", "/resources/sounds/bear.mp3");
            loadSound("hole", "/resources/sounds/hole.mp3");
            loadSound("sled", "/resources/sounds/sled.mp3");
            loadSound("ice", "/resources/sounds/ice.mp3");
            loadSound("event", "/resources/sounds/event.mp3");
            loadSound("win", "/resources/sounds/win.mp3");

        } catch (Exception e) {
            System.err.println("ERROR CRÍTIC: No s'han pogut carregar els recursos d'àudio.");
        }
    }

    /**
     * MÈTODE AUXILIAR PER CARREGAR UN CLIP DE SO INDIVIDUAL.
     */
    private void loadSound(String name, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                sounds.put(name, new AudioClip(url.toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("Advertència: No s'ha trobat l'efecte '" + name + "'.");
        }
    }

    /**
     * REPRODUEIX UN EFECTE DE SO PEL SEU NOM.
     * @param name Identificador del so (ex: "click").
     */
    public void playSound(String name) {
        if (soundEnabled && sounds.containsKey(name)) {
            sounds.get(name).play();
        }
    }

    /**
     * REPRODUEIX UN EFECTE NOMÉS SI NO ESTÀ SONANT JA.
     * Útil per evitar saturació de so en animacions ràpides.
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
     * ATURA LA REPRODUCCIÓ D'UN EFECTE DE SO CONCRET.
     */
    public void stopSound(String name) {
        if (sounds.containsKey(name)) {
            sounds.get(name).stop();
        }
    }

    /**
     * CANVIA LA MÚSICA DE FONS A LA DEL MENÚ.
     * Atura la música actual si és diferent per fer una transició neta.
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
     * CANVIA LA MÚSICA DE FONS A LA DEL JOC.
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
     * ATURA TOTALMENT LA MÚSICA DE FONS.
     */
    public void stopBackgroundMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }

    /**
     * AJUSTA EL VOLUM DE TOTA LA MÚSICA DE FONS.
     * @param volume Valor entre 0.0 i 1.0.
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
     * @param volume Valor entre 0.0 i 1.0.
     */
    public void setSoundVolume(double volume) {
        this.soundVolume = volume;
        for (AudioClip clip : sounds.values()) {
            clip.setVolume(volume);
        }
    }

    /**
     * ACTIVA O DESACTIVA LA REPRODUCCIÓ D'EFECTES DE SO.
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    /**
     * ACTIVA O DESACTIVA LA MÚSICA DE FONS.
     * Si es desactiva, pausa la música actual; si s'activa, la reprèn.
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

    // ── GETTERS DE CONFIGURACIÓ ──────────────────────────────────────────────
    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isMusicEnabled() { return musicEnabled; }
    public double getMusicVolume() { return musicVolume; }
    public double getSoundVolume() { return soundVolume; }
}
