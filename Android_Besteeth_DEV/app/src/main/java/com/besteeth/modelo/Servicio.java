package com.besteeth.modelo;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * Clase Servicio
 *
 * Modelo para crear instancias de un servicio.
 *
 * @author IVAN
 * @version 1.0
 */
public class Servicio implements Parcelable {

    //Atributos
    private int id;
    private String nombre;
    private double precio;

    //Nos sirve para saber si es el falso servicio de ayuda colocado en primer lugar
    // para los spinners Ejemplo --> Seleccionar un servicio
    private boolean servicioPlaceHolder;

    //Constructor
    public Servicio() {
        //
    }

    //Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public boolean isServicioPlaceHolder() {
        return servicioPlaceHolder;
    }

    public void setServicioPlaceHolder(boolean servicioPlaceHolder) {
        this.servicioPlaceHolder = servicioPlaceHolder;
    }

    //To String para los spinners
    @NonNull
    @Override
    public String toString() {
        if (servicioPlaceHolder) {
            return String.format(Locale.getDefault(), "%s", nombre);
        }
        return String.format(Locale.getDefault(), "%s - %.2f€", nombre, precio);
    }

    //Parcelable
    protected Servicio(Parcel in) {
        id = in.readInt();
        nombre = in.readString();
        precio = in.readDouble();
    }

    public static final Creator<Servicio> CREATOR = new Creator<Servicio>() {
        @Override
        public Servicio createFromParcel(Parcel in) {
            return new Servicio(in);
        }

        @Override
        public Servicio[] newArray(int size) {
            return new Servicio[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nombre);
        dest.writeDouble(precio);
    }
}
