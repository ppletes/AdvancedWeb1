package org.foi.nwtis.ppletes.zadaca_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import org.foi.nwtis.ppletes.konfiguracije.Konfiguracija;

public class ZahtjevAviona extends Thread {

    int mojBroj = 0;
    String adresa;
    String komanda;
    int port;
    Konfiguracija konfig;
    int pauza = 3000;

    String avion, polijetanje, slijetanje;

    ZahtjevAviona(int port, String komanda, String avion, String polijetanje, String slijetenje) {
        this.adresa = adresa;
        this.port = port;
        this.komanda = komanda;
        mojBroj = ServerAviona.brojVeze++;
    }

    ZahtjevAviona(int port, String komanda, String avion) {
        this.adresa = adresa;
        this.port = port;
        this.komanda = komanda;
        mojBroj = ServerAviona.brojVeze++;
    }

    @Override
    public void run() {

        try (
                Socket veza = new Socket(adresa, port);
                BufferedReader inps = new BufferedReader(new InputStreamReader(veza.getInputStream(), "UTF-8"));
                OutputStream outs = veza.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(outs);) {

            ispis("Spojen na:  " + port);

            if (komanda.equals("UZLETIO")) {
                osw.write("LET " + avion + "; POLIJETANJE " + polijetanje + "; SLIJETANJE " + slijetanje);
                osw.flush();
            } else if (komanda.equals("ISPIS")) {
                osw.write("POZICIJA " + avion);
                osw.flush();
            }

            veza.shutdownOutput();
            StringBuilder tekst = new StringBuilder();
            while (true) {
                int i = inps.read();
                if (i == -1) {
                    break;
                }
                tekst.append((char) i);
            }
            ispis("Stiglo: " + tekst.toString());
            veza.shutdownInput();
            veza.close();
            Thread.sleep(pauza);
        } catch (SocketException e) {
            ispis(e.getMessage());
        } catch (InterruptedException | IOException ex) {
            ispis(ex.getMessage());
        }
    }

    public synchronized void ispis(String tekst) {
        System.out.println(mojBroj + ". " + tekst);
    }
}
