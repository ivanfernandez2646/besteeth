package com.besteeth.vistamodelo;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;

import com.besteeth.logica.LogInGoogle;
import com.besteeth.logica.LogicaCita;
import com.besteeth.logica.LogicaCliente;
import com.besteeth.logica.LogicaServicio;
import com.besteeth.modelo.Cita;
import com.besteeth.modelo.Cliente;
import com.besteeth.modelo.Configuracion;
import com.besteeth.modelo.FiltroCitas;
import com.besteeth.modelo.Servicio;
import com.besteeth.vista.adaptadores.AdaptadorCitas;
import com.wdullaer.materialdatetimepicker.time.Timepoint;

import java.util.List;

/**
 * Clase CoreViewModel
 * <p>
 * Clase muy útil para la comunicación de CoreActivity con el resto de fragmentos
 * mencionados anteriormente. Permite acceder a los mismos objetos desde distintos fragmentos/activity,
 * comunicar MutableLiveData para refresco de UI. Además, y lo más importante,
 * no se pierde la información hasta que la actividad completa su ciclo de vida
 *
 * @author IVAN
 * @version 1.0
 */
public class CoreViewModel extends AndroidViewModel implements LogicaCliente.LogicaClienteCallBack
        , LogicaCita.LogicaCitaCallBack
        , LogicaServicio.LogicaServicioCallBack {

    //Atributos generales compartidos entre fragmentos
    private String email;
    private int menuSeleccionado;
    private NavController mNavC;
    private SharedPreferences sharedPref;
    private AdaptadorCitas inicio_adaptadorProximasCitas;
    private boolean aplazarCita;
    private Configuracion configuracion;
    private FiltroCitas filtroCitas;
    private boolean pickDialogActivo;

    //MutableLiveData
    private MutableLiveData<Cliente> cliente;
    private MutableLiveData<List<Servicio>> listadoServicios;
    private MutableLiveData<List<Cita>> inicio_listadoProximasCitas;
    private MutableLiveData<Boolean> inicio_altaCita;
    private MutableLiveData<Boolean> inicio_bajaCita;
    private MutableLiveData<Boolean> inicio_aplazarCita;
    private MutableLiveData<Timepoint[]> dlgInsertarCita_listadoHorasOcupadas;
    private MutableLiveData<Boolean> inicio_progressBar;
    private MutableLiveData<List<Cita>> citas_listadoCitas;
    private MutableLiveData<Boolean> miCuenta_resGuardarDatosPersonales;
    private MutableLiveData<Boolean> miCuenta_dlgGuardarDatosPersonales;
    private MutableLiveData<Boolean> miCuenta_existeClienteOdoo;

    //Constructor
    public CoreViewModel(@NonNull Application application) {
        super(application);
    }

    //Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setMenuSeleccionado(int menuSeleccionado) {
        this.menuSeleccionado = menuSeleccionado;
    }

    public int getMenuSeleccionado() {
        return menuSeleccionado;
    }

    public NavController getNavC() {
        return mNavC;
    }

    public void setNavC(NavController mNavC) {
        this.mNavC = mNavC;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AdaptadorCitas getInicio_adaptadorProximasCitas() {
        return inicio_adaptadorProximasCitas;
    }

    public void setInicio_adaptadorProximasCitas(AdaptadorCitas inicio_adaptadorProximasCitas) {
        this.inicio_adaptadorProximasCitas = inicio_adaptadorProximasCitas;
    }

    public boolean isAplazarCita() {
        return aplazarCita;
    }

    public void setAplazarCita(boolean aplazarCita) {
        this.aplazarCita = aplazarCita;
    }

    public Configuracion getConfiguracion() {
        return configuracion;
    }

    public void setConfiguracion(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    public FiltroCitas getFiltroCitas() {
        return filtroCitas;
    }

    public void setFiltroCitas(FiltroCitas filtroCitas) {
        this.filtroCitas = filtroCitas;
    }

    public boolean isPickDialogActivo() {
        return pickDialogActivo;
    }

    public void setPickDialogActivo(boolean pickDialogActivo) {
        this.pickDialogActivo = pickDialogActivo;
    }

    public MutableLiveData<List<Cita>> getInicio_listadoProximasCitas() {
        if (inicio_listadoProximasCitas == null) {
            inicio_listadoProximasCitas = new MutableLiveData<>();
        }
        return inicio_listadoProximasCitas;
    }

    public void setSharedPref(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
    }

    public MutableLiveData<Cliente> getCliente() {
        if (cliente == null) {
            cliente = new MutableLiveData<>();
        }
        return cliente;
    }

    public MutableLiveData<Boolean> getMiCuenta_resGuardarDatosPersonales() {
        if (miCuenta_resGuardarDatosPersonales == null) {
            miCuenta_resGuardarDatosPersonales = new MutableLiveData<>();
        }
        return miCuenta_resGuardarDatosPersonales;
    }

    public MutableLiveData<Boolean> getInicio_altaCita() {
        if (inicio_altaCita == null) {
            inicio_altaCita = new MutableLiveData<>();
        }
        return inicio_altaCita;
    }

    public MutableLiveData<Boolean> getInicio_bajaCita() {
        if (inicio_bajaCita == null) {
            inicio_bajaCita = new MutableLiveData<>();
        }
        return inicio_bajaCita;
    }

    public MutableLiveData<Boolean> getInicio_aplazarCita() {
        if (inicio_aplazarCita == null) {
            inicio_aplazarCita = new MutableLiveData<>();
        }
        return inicio_aplazarCita;
    }

    public MutableLiveData<Timepoint[]> getDlgInsertarCita_listadoHorasOcupadas() {
        if (dlgInsertarCita_listadoHorasOcupadas == null) {
            dlgInsertarCita_listadoHorasOcupadas = new MutableLiveData<>();
        }
        return dlgInsertarCita_listadoHorasOcupadas;
    }

    public MutableLiveData<Boolean> getInicio_progressBar() {
        if (inicio_progressBar == null) {
            inicio_progressBar = new MutableLiveData<>();
        }
        return inicio_progressBar;
    }

    public MutableLiveData<List<Cita>> getCitas_listadoCitas() {
        if (citas_listadoCitas == null) {
            citas_listadoCitas = new MutableLiveData<>();
        }
        return citas_listadoCitas;
    }

    public MutableLiveData<Boolean> getMiCuenta_dlgGuardarDatosPersonales() {
        if (miCuenta_dlgGuardarDatosPersonales == null) {
            miCuenta_dlgGuardarDatosPersonales = new MutableLiveData<>();
        }
        return miCuenta_dlgGuardarDatosPersonales;
    }

    public MutableLiveData<Boolean> getMiCuenta_existeClienteOdoo() {
        if (miCuenta_existeClienteOdoo == null) {
            miCuenta_existeClienteOdoo = new MutableLiveData<>();
        }
        return miCuenta_existeClienteOdoo;
    }

    public MutableLiveData<List<Servicio>> getListadoServicios() {
        if (listadoServicios == null) {
            listadoServicios = new MutableLiveData<>();
        }
        return listadoServicios;
    }

    public void setDlgInsertarCita_listadoHorasOcupadas(MutableLiveData<Timepoint[]> dlgInsertarCita_listadoHorasOcupadas) {
        this.dlgInsertarCita_listadoHorasOcupadas = dlgInsertarCita_listadoHorasOcupadas;
    }

    public void setListadoServicios(MutableLiveData<List<Servicio>> listadoServicios) {
        this.listadoServicios = listadoServicios;
    }

    //Encargado de cerrar la sesión.
    //Se llama tanto cuando cambiamos de email desde MiCuentaFragment, como desde CoreActivity,
    //cuando damos en el menu item cerrar sesión
    public void signOut(Activity activity) {
        LogInGoogle.getInstance().signOutGoogle(activity);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("email", "");
        editor.putBoolean("hasLogin", false);
        editor.apply();
        activity.finish();
    }

    //Lógica ---------------------------------------------------------------------------------------
    public void recuperarProximasCitas() {
        LogicaCita.recuperarProximasCitas(this, email);
    }

    public void recuperarServicios() {
        LogicaServicio.recuperarServicios(this);
    }

    public void altaCita(Cita cita) {
        LogicaCita.altaCita(this, email, cita);
    }

    public void bajaCita(Cita cita) {
        LogicaCita.bajaCita(this, email, cita);
    }

    public void aplazarCita(Cita cita) {
        LogicaCita.aplazarCita(this, email, cita);
    }

    public void recuperarHorasOcupadasDia(String fechaSeleccionada) {
        LogicaCita.recuperarHorasOcupadasDia(this, fechaSeleccionada);
    }

    public void recuperarCitas() {
        LogicaCita.recuperarCitas(this, email, filtroCitas);
    }

    public void recuperarInfoCliente() {
        LogicaCliente.recuperarInfoCliente(this, email);
    }

    public void guardarDatosPersonales(Cliente cliente) {
        LogicaCliente.guardarDatosPersonales(this, cliente, email);
    }

    public void existeClienteOdoo(String nuevoEmail) {
        LogicaCliente.existeClienteEnOdoo(this, nuevoEmail);
    }

    //CallBacks Lógica -----------------------------------------------------------------------------
    @Override
    public void recuperarProximasCitasCallBack(List<Cita> citas) {
        if (inicio_listadoProximasCitas != null) {
            inicio_listadoProximasCitas.setValue(citas);
        }
    }

    @Override
    public void recuperarCitas(List<Cita> citas) {
        if (citas_listadoCitas != null) {
            citas_listadoCitas.setValue(citas);
        }
    }

    @Override
    public void altaCitaCallBack(boolean res) {
        if (inicio_altaCita != null) {
            inicio_altaCita.setValue(res);

            if (inicio_progressBar != null) {
                inicio_progressBar.setValue(false);
            }
        }
    }

    @Override
    public void bajaCitaCallBack(boolean res) {
        if (inicio_bajaCita != null) {
            inicio_bajaCita.setValue(res);

            if (inicio_progressBar != null) {
                inicio_progressBar.setValue(false);
            }
        }
    }

    @Override
    public void aplazarCita(boolean res) {
        if (inicio_aplazarCita != null) {
            inicio_aplazarCita.setValue(res);

            if (inicio_progressBar != null) {
                inicio_progressBar.setValue(false);
            }
        }
    }

    @Override
    public void recuperarHorasOcupadasDiaCallBack(Timepoint[] horasOcupadas, Configuracion configuracion) {
        if (dlgInsertarCita_listadoHorasOcupadas != null) {
            if (configuracion != null) {
                this.configuracion = configuracion;
            }
            dlgInsertarCita_listadoHorasOcupadas.setValue(horasOcupadas);
        }
    }

    @Override
    public void recuperarServiciosCallBack(List<Servicio> listadoServicios) {
        if (this.listadoServicios != null) {
            this.listadoServicios.setValue(listadoServicios);
        }
    }

    @Override
    public void recuperarInfoClienteCallBack(Cliente cliente) {
        if (this.cliente != null) {
            this.cliente.setValue(cliente);
        }
    }

    @Override
    public void guardarDatosPersonalesCallBack(boolean res) {
        if (miCuenta_resGuardarDatosPersonales != null) {
            miCuenta_resGuardarDatosPersonales.setValue(res);
        }
    }

    @Override
    public void existeClienteOdooCallBack(boolean res) {
        if (miCuenta_existeClienteOdoo != null) {
            miCuenta_existeClienteOdoo.setValue(res);
        }
    }

    //Método para setear a nulos los mutables cuando se cambia de fragmento y así evitar problemas de
    //múltiples llamadas a métodos onChange()
    public void setearNulosAtributos() {
        cliente = null;
        inicio_listadoProximasCitas = null;
        inicio_altaCita = null;
        inicio_bajaCita = null;
        inicio_aplazarCita = null;
        inicio_progressBar = null;
        inicio_adaptadorProximasCitas = null;
        listadoServicios = null;
        citas_listadoCitas = null;
        dlgInsertarCita_listadoHorasOcupadas = null;
        miCuenta_resGuardarDatosPersonales = null;
        miCuenta_dlgGuardarDatosPersonales = null;
        miCuenta_existeClienteOdoo = null;
    }


}

