package com.besteeth.modelo;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Clase Cliente
 * <p>
 * Modelo para crear instancias de un Cliente
 *
 * @author IVAN
 * @version 1.0
 */
public class Cliente implements Parcelable {

    //Atributos
    private int id;
    private String dni;
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private double gastosTotales;
    private Bitmap foto;

    //Constructor
    public Cliente() {
        //
    }

    //Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getGastosTotales() {
        return gastosTotales;
    }

    public void setGastosTotales(double gastosTotales) {
        this.gastosTotales = gastosTotales;
    }

    public Bitmap getFoto() {
        return foto;
    }

    public void setFoto(Bitmap foto) {
        this.foto = foto;
    }

    //Parcelable
    protected Cliente(Parcel in) {
        id = in.readInt();
        dni = in.readString();
        nombre = in.readString();
        direccion = in.readString();
        telefono = in.readString();
        email = in.readString();
        gastosTotales = in.readDouble();
        foto = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<Cliente> CREATOR = new Creator<Cliente>() {
        @Override
        public Cliente createFromParcel(Parcel in) {
            return new Cliente(in);
        }

        @Override
        public Cliente[] newArray(int size) {
            return new Cliente[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(dni);
        dest.writeString(nombre);
        dest.writeString(direccion);
        dest.writeString(telefono);
        dest.writeString(email);
        dest.writeDouble(gastosTotales);
        dest.writeParcelable(foto, flags);
    }
}
