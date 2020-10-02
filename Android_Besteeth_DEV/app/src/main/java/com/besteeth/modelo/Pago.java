package com.besteeth.modelo;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Locale;

/**
 * Clase Pago
 * <p>
 * Modelo para crear instancias de un pago para las citas.
 *
 * @author IVAN
 * @version 1.0
 */
public class Pago implements Parcelable {

    //Enumerado con las distintias modalidades de pago
    public enum MODALIDAD_PAGO {
        SIN_FILTRO {
            @NonNull
            @Override
            public String toString() {
                return "Sin Filtro";
            }
        }, //Ninguna restricción
        PAGO_UNICO {
            @NonNull
            @Override
            public String toString() {
                return "Pago Único";
            }
        }, //Solo citas con pago y tipo único
        CUOTAS {
            @NonNull
            @Override
            public String toString() {
                return "Cuotas";
            }
        }, //Solo citas con pagos y tipo cuota
        CUALQUIERA {
            @NonNull
            @Override
            public String toString() {
                return "Cualquiera";
            }
        } //Tienen pagos pero da igual el tipo
    }

    //Atributos
    private DateTime fechaHora;
    private MODALIDAD_PAGO modalidadPago;
    private Double pagoCuota;
    private Integer cantidadCuotas;
    private Integer cuotasPagadas;
    private Integer cuotasRestantes;
    private double pagoTotal;
    private Servicio servicio;

    //Constructor
    public Pago() {
        //
    }

    //Getters and setters
    public String getFechaS() {
        return String.format(Locale.getDefault(), "%02d/%02d/%04d", fechaHora.getDayOfMonth(), fechaHora.getMonthOfYear(), fechaHora.getYear());
    }

    public String getHoraS() {
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", fechaHora.getHourOfDay(), fechaHora.getMinuteOfHour(), fechaHora.getSecondOfMinute());
    }

    public DateTime getFechaHoraUTC() {
        if (fechaHora.getZone() == DateTimeZone.UTC) {
            return fechaHora;
        }
        return new DateTime(fechaHora, DateTimeZone.UTC);
    }

    public DateTime getFechaHoraEuropeMadrid() {
        if (fechaHora.getZone() == DateTimeZone.forID("Europe/Madrid")) {
            return fechaHora;
        }
        return new DateTime(fechaHora, DateTimeZone.forID("Europe/Madrid"));
    }

    public void setFechaHora(DateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public MODALIDAD_PAGO getModalidadPago() {
        return modalidadPago;
    }

    public void setModalidadPago(MODALIDAD_PAGO modalidadPago) {
        this.modalidadPago = modalidadPago;
    }

    public Double getPagoCuota() {
        return pagoCuota;
    }

    public void setPagoCuota(Double pagoCuota) {
        this.pagoCuota = pagoCuota;
    }

    public Integer getCantidadCuotas() {
        return cantidadCuotas;
    }

    public void setCantidadCuotas(Integer cantidadCuotas) {
        this.cantidadCuotas = cantidadCuotas;
    }

    public Integer getCuotasPagadas() {
        return cuotasPagadas;
    }

    public void setCuotasPagadas(Integer cuotasPagadas) {
        this.cuotasPagadas = cuotasPagadas;
    }

    public Integer getCuotasRestantes() {
        return cuotasRestantes;
    }

    public void setCuotasRestantes(Integer cuotasRestantes) {
        this.cuotasRestantes = cuotasRestantes;
    }

    public double getPagoTotal() {
        return pagoTotal;
    }

    public void setPagoTotal(double pagoTotal) {
        this.pagoTotal = pagoTotal;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    //Parcelable
    protected Pago(Parcel in) {
        if (in.readByte() == 0) {
            cantidadCuotas = null;
        } else {
            cantidadCuotas = in.readInt();
        }
        if (in.readByte() == 0) {
            cuotasPagadas = null;
        } else {
            cuotasPagadas = in.readInt();
        }
        if (in.readByte() == 0) {
            cuotasRestantes = null;
        } else {
            cuotasRestantes = in.readInt();
        }
        pagoTotal = in.readDouble();
        servicio = in.readParcelable(Servicio.class.getClassLoader());
    }

    public static final Creator<Pago> CREATOR = new Creator<Pago>() {
        @Override
        public Pago createFromParcel(Parcel in) {
            return new Pago(in);
        }

        @Override
        public Pago[] newArray(int size) {
            return new Pago[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (cantidadCuotas == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(cantidadCuotas);
        }
        if (cuotasPagadas == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(cuotasPagadas);
        }
        if (cuotasRestantes == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(cuotasRestantes);
        }
        dest.writeDouble(pagoTotal);
        dest.writeParcelable(servicio, flags);
    }
}
