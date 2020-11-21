package org.foi.nwtis.ppletes.zadaca_1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.ppletes.konfiguracije.Konfiguracija;
import org.foi.nwtis.ppletes.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.ppletes.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.ppletes.konfiguracije.NemaKonfiguracije;

public class ServerSimulatoraLeta {

    static int brojVeze = 0;
    boolean slijedno = false;
    public static Avioni avioni;

    public static void main(String[] args) {

        ServerSimulatoraLeta posluzitelj = new ServerSimulatoraLeta();
        posluzitelj.izvrsi(args);
    }

    protected void izvrsi(String[] args) {
        Konfiguracija konfig;
        int pauza = 3000;
        String sintaksa = "([^\\s]+\\.(?i)txt|xml) *$";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String p = sb.toString().trim();
        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(p);
        boolean status = m.matches();
        if (!status) {
            System.out.println("Parametri nisu korektno upisani ili niste upisali sve parametre!");
            return;
        }

        String datotekaKonfig = m.group(1);

        try {
            konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(datotekaKonfig);
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            System.out.println("Nema konfiguracijske datoteke!");
            return;
        }

        konfig.dajSvePostavke();

        objektzaKolekcijuLetovaAviona();

        int datPort = Integer.parseInt(konfig.dajPostavku("port.simulator"));
        int datCek = Integer.parseInt(konfig.dajPostavku("maks.cekaca"));

        try {
            ServerSocket server = new ServerSocket(datPort, datCek);

            System.out.println("Čekam vezu na vratima: " + datPort);
            System.out.println("Maks broj čekača: " + datCek);
            System.out.println("\nČekam korisnika da se spoji...");

            //u petlji se ceka da se spoji korisnik -ZahtjevAviona dretva!
            while (true) {
                Socket veza = server.accept();
                Thread dretva = new ZahtjevDretva(veza, pauza);
                dretva.start();
                if (slijedno) {
                    dretva.join();
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerAviona.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void objektzaKolekcijuLetovaAviona() {
        String nazivDatoteke = "avioni.bin";
        try {
            Konfiguracija konf = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
            Properties p = konf.dajSvePostavke();
            for (Object o : p.keySet()) {
                String k = (String) o;
            }
        } catch (NeispravnaKonfiguracija | NemaKonfiguracije ex) {
            System.out.println("Greška kod učitavanja korisnici.xml!" + ex.getMessage());
        }
    }

    class ZahtjevDretva extends Thread {

        int mojBroj = 0;
        Socket veza;
        int pauza = 10000;
        int brojDretvi;
        Konfiguracija konfig;
        String sintaksaKorisnika1 = "(^LET[ a-zA-Z0-9\\.\\_\\-]{3,30})( +POLIJETANJE \\d{4}.\\d{2}.\\d{2} \\d{2}:\\d{2}:\\d{2})( +SLIJETANJE \\d{4}.\\d{2}.\\d{2} \\d{2}:\\d{2}:\\d{2})";
        String sintaksaKorisnika2 = "(^POZICIJA[ a-zA-Z0-9\\.\\_\\-]{3,30})";

        public ZahtjevDretva(Socket veza, int pauza) {
            this.veza = veza;
            mojBroj = ServerAviona.brojVeze++;
            this.pauza = pauza;
        }

        @Override
        public void run() {

            try (InputStream inps = veza.getInputStream(); OutputStream outs = veza.getOutputStream();) {
                StringBuilder tekst = new StringBuilder();

                while (true) {
                    int i = inps.read();
                    if (i == -1) {
                        break;
                    }

                    tekst.append((char) i);

                }
                ispis(tekst.toString());

                //TODO pusta serveru aviona
                veza.shutdownOutput();
                veza.shutdownInput();
                veza.close();
            } catch (SocketException e) {
                ispis(e.getMessage());
            } catch (IOException ex) {
                ispis(ex.getMessage());
            } finally {
                try {
                    Thread.sleep(pauza);
                } catch (InterruptedException ex) {
                    System.exit(0);
                    Logger.getLogger(ServerSimulatoraLeta.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        public synchronized void ispis(String tekst) {
            System.out.println("\nPpletes_SD_" + mojBroj + "  " + tekst);
        }
    }
}
