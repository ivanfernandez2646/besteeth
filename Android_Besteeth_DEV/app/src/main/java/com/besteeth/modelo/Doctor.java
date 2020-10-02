package com.besteeth.modelo;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Clase Doctor
 *
 * Modelo para crear instancias de un Doctor. No utilizada por el momento,
 * pero está preparada ya para futras mejoras de la APP
 *
 * @author IVAN
 * @version 1.0
 */
public class Doctor implements Parcelable {

    //Atributos
    private String dni;
    private String nombre;
    private String apellidos;
    private int edad;
    private double sueldo;
    private Bitmap foto;

    //Constructor
    public Doctor() {
        //
    }

    //Getters and setters
    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public double getSueldo() {
        return sueldo;
    }

    public void setSueldo(double sueldo) {
        this.sueldo = sueldo;
    }

    public Bitmap getFoto() {
        return foto;
    }

    public void setFoto(Bitmap foto) {
        this.foto = foto;
    }

    //Parcelable
    protected Doctor(Parcel in) {
        dni = in.readString();
        nombre = in.readString();
        apellidos = in.readString();
        edad = in.readInt();
        sueldo = in.readDouble();
        foto = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dni);
        dest.writeString(nombre);
        dest.writeString(apellidos);
        dest.writeInt(edad);
        dest.writeDouble(sueldo);
        dest.writeParcelable(foto, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Doctor> CREATOR = new Creator<Doctor>() {
        @Override
        public Doctor createFromParcel(Parcel in) {
            return new Doctor(in);
        }

        @Override
        public Doctor[] newArray(int size) {
            return new Doctor[size];
        }
    };
}
