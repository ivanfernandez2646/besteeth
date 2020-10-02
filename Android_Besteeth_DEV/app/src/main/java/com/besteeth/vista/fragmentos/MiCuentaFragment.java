package com.besteeth.vista.fragmentos;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.besteeth.R;
import com.besteeth.modelo.Cliente;
import com.besteeth.vista.CoreActivity;
import com.besteeth.vista.LoginActivity;
import com.besteeth.vistamodelo.CoreViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickClick;

import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Clase MiCuentaFragment
 * <p>
 * Fragmento encargado de mostrar info acerca del cliente
 * y permitir la edición de la misma. Estos son elementos como su foto,
 * nombre, email, teléfono...
 *
 * @author IVAN
 * @version 1.0
 */
public class MiCuentaFragment extends Fragment {

    public static final int DIALOG_CERRAR_SESION = 1;
    public static final int DIALOG_GUARDAR_DATOS_CLIENTE_EMAIL = 2;

    //Atributos
    private TextView etNombre, etDireccion, etEmail, etTelefono;
    private CircleImageView civFoto;
    private ImageButton ibGuardar, ibSeleccionarFoto;
    private PickSetup pickSetup;
    private ProgressBar pbMiCuenta;
    private SwipeRefreshLayout srMiCuenta;

    private Cliente clienteModificado;

    private CoreViewModel coreVM;

    //Constructor
    public MiCuentaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Inits Observers
        coreVM = new ViewModelProvider(requireActivity()).get(CoreViewModel.class);
        coreVM.getMiCuenta_resGuardarDatosPersonales().hasActiveObservers();
        coreVM.getMiCuenta_resGuardarDatosPersonales().hasObservers();

        coreVM.getCliente().observe(this, new Observer<Cliente>() {
            @Override
            public void onChanged(Cliente cliente) {
                pbMiCuenta.setVisibility(View.INVISIBLE);
                if (cliente != null) {
                    etNombre.setText(cliente.getNombre());
                    etDireccion.setText(cliente.getDireccion());
                    etTelefono.setText(cliente.getTelefono());
                    etEmail.setText(cliente.getEmail());
                    if (cliente.getFoto() != null) {
                        civFoto.setImageBitmap(cliente.getFoto());
                    } else {
                        civFoto.setImageDrawable(getResources().getDrawable(R.drawable.ic_image));
                    }
                } else {
                    Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.msg_RecuperarDatosClienteKO, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        coreVM.getMiCuenta_resGuardarDatosPersonales().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean != null) {
                    pbMiCuenta.setVisibility(View.INVISIBLE);
                    if (aBoolean) {
                        Boolean cambiosEnEmailOK = coreVM.getMiCuenta_dlgGuardarDatosPersonales().getValue();
                        if (cambiosEnEmailOK != null) {
                            if (cambiosEnEmailOK) {
                                coreVM.signOut(requireActivity());
                            }
                        } else {
                            Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.msg_GuardarDatosClienteOK, Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.msg_GuardarDatosClienteKO, Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });

        coreVM.getMiCuenta_dlgGuardarDatosPersonales().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean != null) {
                    if (aBoolean) {
                        pbMiCuenta.setVisibility(View.VISIBLE);
                        coreVM.existeClienteOdoo(clienteModificado.getEmail());
                    }
                }
            }
        });

        coreVM.getMiCuenta_existeClienteOdoo().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean != null) {
                    if (!aBoolean) {
                        coreVM.guardarDatosPersonales(clienteModificado);
                    } else {
                        pbMiCuenta.setVisibility(View.INVISIBLE);
                        Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.msg_EmailYaExiste, Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sign_out, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_mi_cuenta, container, false);

        etNombre = v.findViewById(R.id.etNombre);
        etDireccion = v.findViewById(R.id.etDireccion);
        etTelefono = v.findViewById(R.id.etTelefono);
        etEmail = v.findViewById(R.id.etEmail);
        civFoto = v.findViewById(R.id.civFoto);
        ibGuardar = v.findViewById(R.id.ibGuardar);
        ibSeleccionarFoto = v.findViewById(R.id.ibSeleccionarFoto);
        pbMiCuenta = v.findViewById(R.id.pbMiCuenta);
        srMiCuenta = v.findViewById(R.id.srMiCuenta);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Inits
        pickSetup = new PickSetup()
                .setTitle(getString(R.string.msg_Escoge))
                .setSystemDialog(true);

        pbMiCuenta.setVisibility(View.VISIBLE);

        coreVM.recuperarInfoCliente();

        //Listeners
        ibGuardar.setOnClickListener(ibGuardar_OnClickListener);
        ibSeleccionarFoto.setOnClickListener(ibFoto_OnClickListener);
        srMiCuenta.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                coreVM.recuperarInfoCliente();
                srMiCuenta.setRefreshing(false);
                pbMiCuenta.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //Listener botón de guardado
    private View.OnClickListener ibGuardar_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            if (!faltanDatosObligatorios()) {
                if (regexCorrecto()) {
                    clienteModificado = new Cliente();
                    clienteModificado.setNombre(etNombre.getText().toString());
                    clienteModificado.setDireccion(etDireccion.getText().toString());
                    clienteModificado.setTelefono(etTelefono.getText().toString());
                    clienteModificado.setEmail(etEmail.getText().toString());
                    try {
                        clienteModificado.setFoto(((BitmapDrawable) civFoto.getDrawable()).getBitmap());
                    } catch (Exception e) {
                        clienteModificado.setFoto(null);
                    }

                    //Si hay un cambio en el email, es necesario cerrar la sesión por motivos de seguridad
                    if (!clienteModificado.getEmail().equalsIgnoreCase(coreVM.getEmail())) {
                        Bundle bundleDlgConfirmacion = new Bundle();
                        bundleDlgConfirmacion.putInt("id", DIALOG_GUARDAR_DATOS_CLIENTE_EMAIL);
                        bundleDlgConfirmacion.putInt("titulo", R.string.app_name);
                        bundleDlgConfirmacion.putInt("mensaje", R.string.msg_CambioEmail);
                        coreVM.getNavC().navigate(R.id.dlgConfirmacion, bundleDlgConfirmacion);
                    } else {
                        coreVM.guardarDatosPersonales(clienteModificado);
                        pbMiCuenta.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    };

    //Listener para cambiar la foto del cliente
    private View.OnClickListener ibFoto_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            PickImageDialog.build(pickSetup).show(requireActivity().getSupportFragmentManager());
        }
    };

    //Comprueba los datos obligatorios
    private boolean faltanDatosObligatorios() {
        if (etNombre.getText().toString().equals("") || etDireccion.getText().toString().equals("") || etTelefono.getText().toString().equals("")) {
            Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.msg_FaltanDatosObligatorios, Snackbar.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    //Encargado del tema de expresiones regulares en los campos email
    //y teléfono
    private boolean regexCorrecto() {

        String textError = "";
        String regexEmail = "\\S+@[a-z]+\\.[a-z]+";
        Pattern patternEmail = Pattern.compile(regexEmail);
        String regexTelefono = "[0-9]{9}";
        Pattern patternTelefono = Pattern.compile(regexTelefono);

        boolean emailKO = !patternEmail.matcher(etEmail.getText().toString()).matches();
        if (emailKO) {
            textError += getString(R.string.msg_RegexEmailKO);
        }

        if (!patternTelefono.matcher(etTelefono.getText().toString()).matches()) {
            textError += (emailKO) ? "\n" + getString(R.string.msg_RegexTelefonoKO) : getString(R.string.msg_RegexTelefonoKO);
        }

        if (textError.equals("")) {
            return true;
        }
        Snackbar.make(requireActivity().findViewById(android.R.id.content), getString(R.string.msg_Error) + " " + textError, Snackbar.LENGTH_LONG).show();
        return false;
    }
}
