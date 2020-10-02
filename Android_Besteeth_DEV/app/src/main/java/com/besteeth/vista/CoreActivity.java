package com.besteeth.vista;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.besteeth.R;
import com.besteeth.modelo.Cita;
import com.besteeth.modelo.Cliente;
import com.besteeth.modelo.FiltroCitas;
import com.besteeth.modelo.Pago;
import com.besteeth.vista.dialogos.DlgConfirmacion;
import com.besteeth.vista.dialogos.DlgInformacionCita;
import com.besteeth.vista.dialogos.DlgInsertarCita;
import com.besteeth.vista.fragmentos.InicioFragment;
import com.besteeth.vista.fragmentos.MiCuentaFragment;
import com.besteeth.vistamodelo.CoreViewModel;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.listeners.IPickResult;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Clase CoreActivity
 * <p>
 * Actividad principal ejecutada tras LoginActivity, encargada de administrar
 * el BottomAppBar, los callbacks de los diálogos de los distinos fragmentos y
 * posee un espacio en el cual se van mostrando y ocultando los fragmentos principales
 * de la app
 *
 * @author IVAN
 * @version 1.0
 */
public class CoreActivity extends AppCompatActivity implements IPickResult, DlgConfirmacion.DlgConfirmacionListener, DlgInformacionCita.DlgInformacionCitaListener, DlgInsertarCita.DlgInsertarCitaListener {

    //Atributos
    private NavController mNavC;
    private CoreViewModel coreVM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.custom_action_bar);
        }
        setContentView(R.layout.activity_core);

        //Inits
        coreVM = new ViewModelProvider(this).get(CoreViewModel.class);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (getIntent() != null) {
            String email = getIntent().getStringExtra("email");
            CoreViewModel coreVM = new ViewModelProvider(this).get(CoreViewModel.class);
            coreVM.setEmail(email);
        }

        //FindViewByIds
        BottomAppBar bottomAppBar = findViewById(R.id.bottom_app_bar);
        FloatingActionButton fabInsertarCita = findViewById(R.id.fabInsertarCita);
        mNavC = Navigation.findNavController(this, R.id.navhostfrag_core);

        //Listeners
        bottomAppBar.setOnMenuItemClickListener(bottomAppBar_OnMenuItemClickListener);
        fabInsertarCita.setOnClickListener(fabInsertarCita_OnClickListener);

        coreVM.setNavC(mNavC);
        coreVM.setSharedPref(sharedPref);
        coreVM.setMenuSeleccionado(R.id.menuItem_Inicio);


        //Seteamos FiltroCitas en el CoreVM
        FiltroCitas filtroCitas = new FiltroCitas();
        final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        DateTime fechaFiltro = new DateTime(mYear, mMonth + 1, 1, 0, 0, 0);
        filtroCitas.setFecha(fechaFiltro);
        filtroCitas.setServicio(null);
        filtroCitas.setCerrada(false);
        filtroCitas.setModalidadPago(Pago.MODALIDAD_PAGO.CUOTAS);
        coreVM.setFiltroCitas(filtroCitas);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    //Sobreescribimoos el método onPause() para que en caso de salir de la app
    //al volver a entrar compruebe de nuevo el login y que el usuario está presente en la BBDD
    //para así ahorrarnos problemas
    @Override
    protected void onPause() {
        super.onPause();
        //Si es igual a 1, es que solamente contienen el NavHostFragment
        //Si es mayor a 1, es que se encuentra ejecutando PickImageDialog, ya que este no está incluido
        //en el navhostfragment
        if(getSupportFragmentManager().getFragments().size() == 1){
            finish();
        }
    }

    //Controlaremos que si estamos en el fragmento InicioFragment,
    //saldremos cerraremos la actividad, para que así cuando volvamos a entrar,
    //se pase por el login
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (coreVM.getMenuSeleccionado() == R.id.menuItem_Inicio) {
            finishAffinity();
        } else {
            switch (Objects.requireNonNull(coreVM.getNavC().getCurrentDestination()).getId()) {
                case R.id.inicioFragment:
                    coreVM.setMenuSeleccionado(R.id.menuItem_Inicio);
                    break;
                case R.id.citasFragment:
                    coreVM.setMenuSeleccionado(R.id.menuItem_Citas);
                    break;
                case R.id.miCuentaFragment:
                    coreVM.setMenuSeleccionado(R.id.menuItem_MiCuenta);
                    break;
            }
        }
    }

    //Listener BottomAppBar
    private BottomAppBar.OnMenuItemClickListener bottomAppBar_OnMenuItemClickListener = new BottomAppBar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() != coreVM.getMenuSeleccionado()) {
                coreVM.setearNulosAtributos();
                mNavC.navigateUp();

                if (coreVM.getMenuSeleccionado() == R.id.menuItem_FiltroCitas) {
                    mNavC.navigateUp();
                }

                if (item.getItemId() == R.id.menuItem_Citas) {
                    mNavC.navigate(R.id.action_inicioFragment_to_citasFragment);
                } else if (item.getItemId() == R.id.menuItem_MiCuenta) {
                    mNavC.navigate(R.id.action_inicioFragment_to_miCuentaFragment);
                }
                coreVM.setMenuSeleccionado(item.getItemId());
            }
            return true;
        }
    };

    //Listener botón añadir cita
    private View.OnClickListener fabInsertarCita_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (coreVM.getMenuSeleccionado() != R.id.menuItem_Inicio) {
                coreVM.setearNulosAtributos();
                mNavC.navigateUp();
                if (coreVM.getMenuSeleccionado() == R.id.menuItem_FiltroCitas) {
                    mNavC.navigateUp();
                }
                coreVM.setMenuSeleccionado(R.id.menuItem_Inicio);
            }
            coreVM.setAplazarCita(false);
            mNavC.navigate(R.id.dlgInsertarCita);
        }
    };

    //CallBack menús del AppBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItem_SignOut:
                Bundle bundleDlgConfirmacion = new Bundle();
                bundleDlgConfirmacion.putInt("id", MiCuentaFragment.DIALOG_CERRAR_SESION);
                bundleDlgConfirmacion.putInt("titulo", R.string.app_name);
                bundleDlgConfirmacion.putInt("mensaje", R.string.msg_CerrarSesion);
                mNavC.navigate(R.id.dlgConfirmacion, bundleDlgConfirmacion);
                break;
            case R.id.menuItem_FiltroCitas:
                coreVM.setMenuSeleccionado(item.getItemId());
                mNavC.navigate(R.id.action_citasFragment_to_filtroCitasFragment);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //CallBack image picker dialog MiCuentaFragment OK
    @Override
    public void onPickResult(PickResult r) {
        if (r.getBitmap() != null) {
            CoreViewModel miCuentaVM = new ViewModelProvider(this).get(CoreViewModel.class);
            Cliente cliente = miCuentaVM.getCliente().getValue();
            cliente.setFoto(r.getBitmap());
            miCuentaVM.getCliente().setValue(cliente);
        }
    }


    //CallBack DlgConfirmacion positive click, trae un id
    //para distinguir el ámbito y la actividad a realizar
    @Override
    public void onDlgConfirmacionPositiveClick(DialogFragment dialog, int mId) {
        switch (mId) {
            case MiCuentaFragment.DIALOG_CERRAR_SESION:
                coreVM.signOut(this);
                break;
            case MiCuentaFragment.DIALOG_GUARDAR_DATOS_CLIENTE_EMAIL:
                coreVM.getMiCuenta_dlgGuardarDatosPersonales().setValue(true);
                break;
            case InicioFragment.DIALOG_CONFIRMAR_BORRADO_CITA:
                coreVM.getInicio_progressBar().setValue(true);
                if (dialog.getArguments() != null) {
                    coreVM.bajaCita(dialog.getArguments().getParcelable("cita"));
                }
                break;
        }
    }

    //CallBack DlgConfirmacion negative click, trae un id
    //para distinguir el ámbito y la actividad a realizar
    @Override
    public void onDlgConfirmacionNegativeClick(DialogFragment dialog, int mId) {
        switch (mId) {
            case InicioFragment.DIALOG_CONFIRMAR_BORRADO_CITA:
                if (dialog.getArguments() != null) {
                    coreVM.getInicio_adaptadorProximasCitas().notifyItemChanged(dialog.getArguments().getInt("posicion"));
                }
                break;
        }
    }

    //CallBack DlgInsertar positive click, trae una cita
    //que es la que va a ser actualizada o añadida
    @Override
    public void onDlgInsertarCitaPositiveClick(DialogFragment dialog, Cita cita) {
        coreVM.getInicio_progressBar().setValue(true);
        if (coreVM.isAplazarCita()) {
            coreVM.aplazarCita(cita);
        } else {
            coreVM.altaCita(cita);
        }
        coreVM.setConfiguracion(null);
        coreVM.setDlgInsertarCita_listadoHorasOcupadas(null);
        coreVM.setListadoServicios(null);
    }

    //CallBack DlgInsertar negative click
    @Override
    public void onDlgInsertarCitaNegativeClick(DialogFragment dialog) {
        if (coreVM.isAplazarCita()) {
            if (dialog.getArguments() != null) {
                int posicionElementoSeleccionado = dialog.getArguments().getInt("posicion");
                coreVM.getInicio_adaptadorProximasCitas().notifyItemChanged(posicionElementoSeleccionado);
            }
        }
        coreVM.setConfiguracion(null);
        coreVM.setDlgInsertarCita_listadoHorasOcupadas(null);
        coreVM.setListadoServicios(null);
    }

    //CallBack DlgInformacionCita negative click, en principio
    //no posee ninguna funcionalidad
    @Override
    public void onDlgInformacionCitaPositiveClick(DialogFragment dialog) {
        //
    }
}
