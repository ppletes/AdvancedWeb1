package org.foi.nwtis.ppletes.zadaca_1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import static java.lang.Thread.sleep;
import org.foi.nwtis.ppletes.konfiguracije.Konfiguracija;

class Avioni implements Serializable {

    String avion;
    String vrijeme;
    String aerodromP;
    String aerodromS;
    String poljetanja;
    String sljetanja;

    Konfiguracija konfig;

    public Avioni(String avion, String aerodromP, String poljetanja, String aerodromS, String sljetanja) {//

        this.poljetanja = poljetanja;
        this.sljetanja = sljetanja;
        this.avion = avion;
        this.aerodromS = aerodromS;
        this.aerodromP = aerodromP;

    }

    public String getAvion() {
        return avion;
    }

    public void setAvion(String avion) {
        this.avion = avion;
    }

    public String getAerodromS() {
        return aerodromS;
    }

    public void setAerodromS(String aerodromS) {
        this.aerodromS = aerodromS;
    }

    public String getAerodromP() {
        return aerodromP;
    }

    public void setAerodromP(String aerodromP) {
        this.aerodromP = aerodromP;
    }

    public String getPoljetanja() {
        return poljetanja;
    }

    public void setPoljetanja(String poljetanja) {
        this.poljetanja = poljetanja;
    }

    public String getSljetanja() {
        return sljetanja;
    }

    public void setSljetanja(String sljetanja) {
        this.sljetanja = sljetanja;
    }

    @Override
    public String toString() {
        return "\nLET [Avion: " + avion + "| Aerodrom polijetanja: " + aerodromP + "| Vrijeme poljetanja: " + poljetanja + "| Aerodrom slijetanja: " + aerodromS + "| Vrijeme sljetanja: " + sljetanja + "]";
    }
}

public class ServisAviona extends Thread {

    Konfiguracija konfig;
    Avioni avioni;
    private boolean zaustavi = false;

    public ServisAviona(Konfiguracija konfig, Avioni avioni) {
        this.konfig = konfig;
        this.avioni = avioni;

    }

    @Override
    public void run() {
        super.run();

        int intervalSerijalizacije = Integer.parseInt(konfig.dajPostavku("interval.pohrane.aviona"));

        try {
            Avioni avion = (Avioni) deSerialization();
            
            avioni = new Avioni("AVION-999", "EEL", "2020.02.02 15:30:00", "GGN", "2020.02.02 20:30:00");

        } catch (IOException | ClassNotFoundException ex) {

        }

        while (!zaustavi) {
            long poc = System.currentTimeMillis();

            try {
                serialization(avioni);
                Avioni object = (Avioni) deSerialization();
                
                System.out.println(object.toString());

            } catch (IOException | ClassNotFoundException exp) {
            }

            long trajanje = System.currentTimeMillis() - poc;
            try {
                sleep(intervalSerijalizacije * 1000 - trajanje); // x1000 jer je postavka spremljena u sekundama
            } catch (InterruptedException ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }

    }

    public void zaustaviSerijalizacijuEvidencije() {
        this.zaustavi = true;
    }

    public static void serialization(Avioni avion) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("avioni.bin");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(avion);
        objectOutputStream.close();
    }

    public static Object deSerialization() throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("avioni.bin");
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        Avioni avion = (Avioni) objectInputStream.readObject();
        objectInputStream.close();
        return avion;
    }
}
