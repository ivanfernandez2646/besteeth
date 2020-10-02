package com.besteeth.modelo;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.LocalTime;

/**
 * Clase Configuración
 *
 * Modelo para crear instancias de la Configuración
 *
 * @author IVAN
 * @version 1.0
 */
public class Configuracion implements Parcelable {

    //Atributos
    private LocalTime horaApertura;
    private LocalTime horaCierre;
    private int intervaloMinutosCita;

    //Constructor
    public Configuracion(){
        //
    }

    //Getters and setters
    public LocalTime getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(LocalTime horaApertura) {
        this.horaApertura = horaApertura;
    }

    public LocalTime getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(LocalTime horaCierre) {
        this.horaCierre = horaCierre;
    }

    public int getIntervaloMinutosCita() {
        return intervaloMinutosCita;
    }

    public void setIntervaloMinutosCita(int intervaloMinutosCita) {
        this.intervaloMinutosCita = intervaloMinutosCita;
    }

    //Parcelable
    protected Configuracion(Parcel in) {
        intervaloMinutosCita = in.readInt();
    }

    public static final Creator<Configuracion> CREATOR = new Creator<Configuracion>() {
        @Override
        public Configuracion createFromParcel(Parcel in) {
            return new Configuracion(in);
        }

        @Override
        public Configuracion[] newArray(int size) {
            return new Configuracion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(intervaloMinutosCita);
    }
}
