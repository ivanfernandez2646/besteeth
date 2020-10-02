package com.besteeth.modelo;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

import java.util.Locale;

/**
 * Clase FiltroCitas
 *
 * Modelo para crear instancias de un filtro para las citas.
 *
 * @author IVAN
 * @version 1.0
 */
public class FiltroCitas implements Parcelable {

    //Atributos
    private DateTime fecha;
    private Servicio servicio;
    private boolean cerrada;
    private Pago.MODALIDAD_PAGO modalidadPago;

    //Constructor
    public FiltroCitas() {
        //
    }

    //Getters and setters
    public DateTime getFecha() {
        return fecha;
    }

    public String getFechaS() {
        return String.format(Locale.getDefault(), "%02d/%02d/%04d",fecha.getDayOfMonth(),fecha.getMonthOfYear(),fecha.getYear());
    }

    public void setFecha(DateTime fecha) {
        this.fecha = fecha;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    public boolean isCerrada() {
        return cerrada;
    }

    public void setCerrada(boolean cerrada) {
        this.cerrada = cerrada;
    }

    public Pago.MODALIDAD_PAGO getModalidadPago() {
        return modalidadPago;
    }

    public void setModalidadPago(Pago.MODALIDAD_PAGO modalidadPago) {
        this.modalidadPago = modalidadPago;
    }

    //Parcelable
    protected FiltroCitas(Parcel in) {
        servicio = in.readParcelable(Servicio.class.getClassLoader());
        cerrada = in.readByte() != 0;
    }

    public static final Creator<FiltroCitas> CREATOR = new Creator<FiltroCitas>() {
        @Override
        public FiltroCitas createFromParcel(Parcel in) {
            return new FiltroCitas(in);
        }

        @Override
        public FiltroCitas[] newArray(int size) {
            return new FiltroCitas[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(servicio, flags);
        dest.writeByte((byte) (cerrada ? 1 : 0));
    }
}
