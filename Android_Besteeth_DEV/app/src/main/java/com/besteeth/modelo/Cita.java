package com.besteeth.modelo;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Clase Cita
 *
 * Modelo para crear instancias de una Cita
 *
 * @author IVAN
 * @version 1.0
 */
public class Cita implements Parcelable{

    //Atributos
    private int id;
    private DateTime fechaHora;
    private boolean cerrada;
    private String nameDoctor;
    private Cliente cliente;
    private Servicio servicio;
    private List<Pago> pagos;
    private boolean pagoIniciado;
    private boolean atendida;

    //Utilizado para cuando queremos aplazar una cita
    private DateTime fechaHoraAntigua;

    //Constructor
    public Cita() {
        pagos = new ArrayList<>();
    }

    //Getters and setters
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getFechaS() {
        return String.format(Locale.getDefault(), "%02d/%02d/%04d",fechaHora.getDayOfMonth(),fechaHora.getMonthOfYear(),fechaHora.getYear());
    }

    public String getHoraS() {
        return String.format(Locale.getDefault(), "%02d:%02d",fechaHora.getHourOfDay(),fechaHora.getMinuteOfHour());
    }

    public DateTime getFechaHoraUTC() {
        if(fechaHora.getZone() == DateTimeZone.UTC){
            return fechaHora;
        }
        return new DateTime(fechaHora, DateTimeZone.UTC);
    }

    public DateTime getFechaHoraEuropeMadrid(){
        if(fechaHora.getZone() == DateTimeZone.forID("Europe/Madrid")){
            return fechaHora;
        }
        return new DateTime(fechaHora, DateTimeZone.forID("Europe/Madrid"));
    }

    public String getFechaHoraUTCString(){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        return dateTimeFormatter.print(getFechaHoraUTC());
    }

    public String getFechaHoraEuropeMadridString(){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        return dateTimeFormatter.print(getFechaHoraEuropeMadrid());
    }

    public void setFechaHora(DateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public boolean isCerrada() {
        return cerrada;
    }

    public void setCerrada(boolean cerrada) {
        this.cerrada = cerrada;
    }

    public String getNameDoctor() {
        return nameDoctor;
    }

    public void setNameDoctor(String nameDoctor) {
        this.nameDoctor = nameDoctor;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    public List<Pago> getPagos() {
        return pagos;
    }

    public void setPagos(List<Pago> pagos) {
        this.pagos = pagos;
    }

    public boolean isPagoIniciado() {
        return pagoIniciado;
    }

    public void setPagoIniciado(boolean pagoIniciado) {
        this.pagoIniciado = pagoIniciado;
    }

    public boolean isAtendida() {
        return atendida;
    }

    public void setAtendida(boolean atendida) {
        this.atendida = atendida;
    }

    public DateTime getFechaHoraAntiguaUTC() {
        if(fechaHoraAntigua.getZone() == DateTimeZone.UTC){
            return fechaHoraAntigua;
        }
        return new DateTime(fechaHoraAntigua, DateTimeZone.UTC);
    }

    public DateTime getFechaHoraAntiguaEuropeMadrid(){
        if(fechaHoraAntigua.getZone() == DateTimeZone.forID("Europe/Madrid")){
            return fechaHoraAntigua;
        }
        return new DateTime(fechaHoraAntigua, DateTimeZone.forID("Europe/Madrid"));
    }

    public String getFechaHoraAntiguaUTCString(){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        return dateTimeFormatter.print(getFechaHoraAntiguaUTC());
    }

    public String getFechaHoraAntiguaEuropeMadridString(){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        return dateTimeFormatter.print(getFechaHoraAntiguaEuropeMadrid());
    }

    public void setFechaHoraAntigua(DateTime fechaHoraAntigua) {
        this.fechaHoraAntigua = fechaHoraAntigua;
    }

    //Parcelable
    protected Cita(Parcel in) {
        id = in.readInt();
        cerrada = in.readByte() != 0;
        nameDoctor = in.readString();
        cliente = in.readParcelable(Cliente.class.getClassLoader());
        servicio = in.readParcelable(Servicio.class.getClassLoader());
        pagos = in.createTypedArrayList(Pago.CREATOR);
        pagoIniciado = in.readByte() != 0;
        atendida = in.readByte() != 0;
    }

    public static final Creator<Cita> CREATOR = new Creator<Cita>() {
        @Override
        public Cita createFromParcel(Parcel in) {
            return new Cita(in);
        }

        @Override
        public Cita[] newArray(int size) {
            return new Cita[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeByte((byte) (cerrada ? 1 : 0));
        dest.writeString(nameDoctor);
        dest.writeParcelable(cliente, flags);
        dest.writeParcelable(servicio, flags);
        dest.writeTypedList(pagos);
        dest.writeByte((byte) (pagoIniciado ? 1 : 0));
        dest.writeByte((byte) (atendida ? 1 : 0));
    }
}
