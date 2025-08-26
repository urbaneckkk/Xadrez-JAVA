package view;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundPlayer {
    
    // Caminhos para os arquivos de som
    private static final String MOVE_SOUND = "/resources/sounds/move.wav";
    private static final String CAPTURE_SOUND = "/resources/sounds/capture.wav";
    
    /**
     * Reproduz um som de movimento ou captura
     * @param isCapture true se for uma captura, false se for um movimento normal
     */
    public static void playSound(boolean isCapture) {
        String soundFile = isCapture ? CAPTURE_SOUND : MOVE_SOUND;
        
        try {
            // Obtém o URL do recurso
            URL soundURL = SoundPlayer.class.getResource(soundFile);
            
            if (soundURL == null) {
                System.err.println("Não foi possível encontrar o arquivo de som: " + soundFile);
                return;
            }
            
            // Obtém um stream de áudio
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL);
            
            // Obtém um clip de áudio
            Clip clip = AudioSystem.getClip();
            
            // Abre o clip com o stream de áudio
            clip.open(audioInputStream);
            
            // Adiciona um listener para fechar recursos quando o som terminar
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    try {
                        audioInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            
            // Reproduz o som
            clip.start();
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Erro ao reproduzir som: " + e.getMessage());
        }
    }
}