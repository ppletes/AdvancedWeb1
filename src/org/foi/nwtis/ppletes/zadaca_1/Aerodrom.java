package org.foi.nwtis.ppletes.zadaca_1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Aerodrom {

    String icao;
    String naziv;
    String drzava;
    String koordinate;

    public Aerodrom(String icao, String naziv, String drzava, String koordinate) throws IOException {
        this.icao = icao;
        this.naziv = naziv;
        this.drzava = drzava;
        this.koordinate = koordinate;

    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public String getDrzava() {
        return drzava;
    }

    public void setDrzava(String drzava) {
        this.drzava = drzava;
    }

    public String getKoordinate() {
        return koordinate;
    }

    public void setKoordinate(String koordinate) {
        this.koordinate = koordinate;
    }

    @Override
    public String toString() {
        return "\nAERODROM [Icao: " + icao + "| Naziv: " + naziv + "| Drzava: " + drzava + "| Koordinate: " + koordinate + "]";
    }

    public static List<Aerodrom> readAerodrom() throws FileNotFoundException, IOException {
        List<Aerodrom> list = new ArrayList<Aerodrom>();
        try (Scanner reader = new Scanner(new File("aerodromi.csv"))) {
            reader.nextLine();
            while (reader.hasNextLine()) {
                String[] tempArray = reader.nextLine().split(",");
                Aerodrom e = new Aerodrom(tempArray[0], tempArray[1], tempArray[2], tempArray[3]);
                list.add(e);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
