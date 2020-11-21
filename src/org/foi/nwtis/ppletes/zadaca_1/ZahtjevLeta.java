package org.foi.nwtis.ppletes.zadaca_1;

public class ZahtjevLeta {

    String avion;
    String polijetanje;
    String slijetanje;

    public ZahtjevLeta(String avion, String polijetanje, String slijetanje) {
        this.avion = avion;
        this.polijetanje = polijetanje;
        this.slijetanje = slijetanje;
    }

    public String getAvion() {
        return avion;
    }

    public void setAvion(String avion) {
        this.avion = avion;
    }

    public String getPolijetanje() {
        return polijetanje;
    }

    public void setPolijetanje(String polijetanje) {
        this.avion = polijetanje;
    }

    public String getSlijetanje() {
        return avion;
    }

    public void setSlijetanje(String slijetanje) {
        this.avion = slijetanje;
    }

    @Override
    public String toString() {
        return "\nLet [Icao: " + avion + "| Vrijeme polijetanja: " + polijetanje + "| Vrijeme slijetanja: " + slijetanje + "]";
    }

    public void leti() {
        //TODO upit za prosljedenog aviona s obzirom na vrijeme
    }

    public void sletio() {
        //TODO upit za prosljedenog aviona s obzirom na vrijeme
        //avion.datumVrijeme<trenutno.datumVrijeme
    }
}
