package org.foi.nwtis.ppletes.zadaca_1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.ppletes.konfiguracije.Konfiguracija;

public class KorisnikAviona {

    static int brojVeze = 0;

    public static void main(String[] args) {

        KorisnikAviona klijent = new KorisnikAviona();
        klijent.izvrsi(args);
    }
    private final static Pattern TEST_PATTERN = Pattern.compile("</??tag(\\s+?.*?)??>", Pattern.CASE_INSENSITIVE);

    protected void izvrsi(String[] args) {
        Konfiguracija konf;
        int pauza = 1000;
        int brojDretvi;

        String sintaksa = "(^+-k[ a-zA-Z0-9\\_\\-]{3,10})( +-l[ a-zA-Z0-9\\_\\-]{3,10})( +-s[ a-zA-Z0-9\\.\\_\\-]{3,30}|(?:[0-9]{1,3}\\.){3}[0-9]{1,3})( +-p (?:900[0-9]|90[1-9][0-9]|9[1-9][0-9]{2}))( +(?:--kraj|--ispis[ a-zA-Z0-9\\_\\-]{3,30}|--dodajDretve (?:[1-9]|1[0-9]|20)|--uzletio \"AerodromPolazište:[ a-zA-Z0-9\\_\\-]{3,10}, AerodromOdredište:[ a-zA-Z0-9\\_\\-]{3,10}, Avion:[ a-zA-Z0-9\\_\\-]{3,30}, trajanjeLeta:[ 0-9\"]*$))";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String p = sb.toString().trim();
        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(p);
        boolean status = m.matches();
        if (!status) {
            System.out.println("Parametri nisu korektno upisani!");
            return;
        }
        if (m.group(1) == null || m.group(2) == null || m.group(3) == null || m.group(4) == null || m.group(5) == null) {
            System.out.println("Nisu upisani svi parametri!");
            return;
        }

        String adresa = args[5];
        String port = args[7];
        String lozinka = args[3];
        String ime = args[1];
        String komanda = args[8];

        if (m.group(5).contains("--dodajDretve")) {
            String parametar = args[9];
            brojDretvi = Integer.parseInt(parametar);
            System.out.println("Otvaram vezu na: " + adresa + " | Port: " + Integer.parseInt(port));
            System.out.println("Šaljem komandu: DODAJ" + Integer.parseInt(args[9]));
            Thread dretva = new KlijentDretva(ime, lozinka, adresa, Integer.parseInt(port), komanda, brojDretvi);
            dretva.start();
        }
        if (m.group(5).contains("--kraj")) {
            System.out.println("Otvaram vezu na: " + adresa + " | Port: " + Integer.parseInt(port));
            System.out.println("Šaljem komandu: KRAJ");
            Thread dretva = new KlijentDretva(ime, lozinka, adresa, Integer.parseInt(port), komanda);
            dretva.start();

        }
        if (m.group(5).contains("--uzletio")) {
            String avion = args[14].substring(0, args[14].length() - 1);
            String polaziste = args[10].substring(0, args[10].length() - 1);
            String odrediste = args[12].substring(0, args[12].length() - 1);
            String trajanje = args[16].substring(0, args[16].length() - 1);
            System.out.println("Otvaram vezu na: " + adresa + " | Port: " + Integer.parseInt(port));
            System.out.println("Šaljem komandu: UZLETIO");

            Thread dretva = new KlijentDretva(ime, lozinka, adresa, Integer.parseInt(port), komanda, avion, polaziste, odrediste, trajanje);
            dretva.start();

        }
        if (m.group(5).contains("--ispis")) {
            String icaoAvion = args[9];
            System.out.println("Otvaram vezu na: " + adresa + " | Port: " + Integer.parseInt(port));
            System.out.println("Šaljem komandu: ISPIS");

            Thread dretva = new KlijentDretva(ime, lozinka, adresa, Integer.parseInt(port), komanda, icaoAvion);
            dretva.start();

        }

    }

    class KlijentDretva extends Thread {

        int mojBroj = 0;
        int port;
        int pauza = 3000;
        int brojDretvi;

        String ime;
        String lozinka;
        String adresa;
        String parametar;
        String komanda;
        String avion;
        String polaziste;
        String odrediste;
        String trajanje;

        public KlijentDretva(String ime, String lozinka, String adresa, int port, String komanda, int brojDretvi) {
            this.port = port;
            this.ime = ime;
            this.lozinka = lozinka;
            this.adresa = adresa;
            this.brojDretvi = brojDretvi;
            this.komanda = komanda;

            this.pauza = pauza;
            mojBroj = KorisnikAviona.brojVeze++;
        }

        public KlijentDretva(String ime, String lozinka, String adresa, int port, String komanda) {
            this.port = port;
            this.ime = ime;
            this.lozinka = lozinka;
            this.adresa = adresa;
            this.parametar = parametar;
            this.komanda = komanda;

            this.pauza = pauza;
            mojBroj = KorisnikAviona.brojVeze++;
        }

        public KlijentDretva(String ime, String lozinka, String adresa, int port, String komanda, String parametar) {
            this.port = port;
            this.ime = ime;
            this.lozinka = lozinka;
            this.adresa = adresa;
            this.parametar = parametar;
            this.komanda = komanda;

            this.pauza = pauza;
            mojBroj = KorisnikAviona.brojVeze++;
        }

        public KlijentDretva(String ime, String lozinka, String adresa, int port, String komanda, String avion, String polaziste, String odrediste, String trajanje) {
            this.port = port;
            this.ime = ime;
            this.lozinka = lozinka;
            this.adresa = adresa;
            this.komanda = komanda;
            this.avion = avion;
            this.polaziste = polaziste;
            this.odrediste = odrediste;
            this.trajanje = trajanje;

            this.pauza = pauza;
            mojBroj = KorisnikAviona.brojVeze++;
        }

        @Override
        public void run() {
            try (
                    Socket veza = new Socket(adresa, port);
                    BufferedReader inps = new BufferedReader(new InputStreamReader(veza.getInputStream(), "UTF-8"));
                    OutputStream outs = veza.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(veza.getOutputStream(), Charset.forName("UTF-8").newEncoder());) {

                StringBuilder tekst = new StringBuilder();

                if (komanda.equals("--kraj")) {
                    osw.write("KORISNIK " + ime + "; LOZINKA " + lozinka + "; KRAJ" + ";");
                    osw.flush();
                } else if (komanda.equals("--dodajDretve")) {
                    osw.write("KORISNIK " + ime + "; LOZINKA " + lozinka + "; DODAJ " + brojDretvi);
                    osw.flush();
                } else if (komanda.equals("--uzletio")) {
                    osw.write("KORISNIK " + ime + "; LOZINKA " + lozinka + "; UZLETIO " + avion + "; POLAZIŠTE " + polaziste + "; ODREDIŠTE " + odrediste + "; TRAJANJE " + trajanje + ";");
                    osw.flush();
                } else if (komanda.equals("--ispis")) {
                    osw.write("KORISNIK " + ime + "; LOZINKA " + lozinka + "; ISPIS " + parametar + ";");
                    osw.flush();
                }
                veza.shutdownOutput();
                while (true) {
                    int i = inps.read();
                    if (i == -1) {
                        break;
                    }
                    tekst.append((char) i);
                }
                ispis("Odgovor: " + tekst.toString());

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
            System.out.println("\nPpletes_SD_" + mojBroj + "  " + tekst);
        }
    }
}
