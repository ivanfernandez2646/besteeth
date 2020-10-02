package com.besteeth.vista.fragmentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.besteeth.R;
import com.besteeth.modelo.FiltroCitas;
import com.besteeth.modelo.Pago;
import com.besteeth.modelo.Servicio;
import com.besteeth.vistamodelo.CoreViewModel;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Clase FiltroCitasFragment
 *
 * Fragmento encargado de proveer diferentes elementos visuales
 * para modificar el estado del FiltroCitas actual
 *
 * @author IVAN
 * @version 1.0
 */
public class FiltroCitasFragment extends Fragment implements com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener {

    //Atributos
    private EditText etFiltroFechaDlg;
    private Switch swFiltroCerradaDlg;
    private Spinner spFiltroModalidadPagoDlg, spFiltroServicioDlg;
    private Button btFiltroFechaDlg, btCancelar, btAceptar;
    private ProgressBar pbFiltroCitas;

    private ArrayAdapter mAdaptadorSpFiltroModalidadPago;

    private CoreViewModel coreVM;
    private FiltroCitas filtroCitas;

    //Constructor
    public FiltroCitasFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        //Init Adaptador y ViewModel
        coreVM = new ViewModelProvider(requireActivity()).get(CoreViewModel.class);

        coreVM.getListadoServicios().observe(this, new Observer<List<Servicio>>() {
            @Override
            public void onChanged(List<Servicio> servicios) {
                pbFiltroCitas.setVisibility(View.INVISIBLE);
                if (servicios != null) {
                    //Init Spinner
                    ArrayAdapter<Servicio> adapter = new ArrayAdapter<Servicio>(requireContext(), android.R.layout.simple_spinner_item, servicios);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spFiltroServicioDlg.setAdapter(adapter);

                    if (filtroCitas.getServicio() != null) {

                        for (Servicio servicio : servicios) {
                            if (servicio.getId() == filtroCitas.getServicio().getId()) {
                                filtroCitas.setServicio(servicio);
                            }
                        }

                        spFiltroServicioDlg.setSelection(adapter.getPosition(filtroCitas.getServicio()));
                    } else {
                        spFiltroServicioDlg.setSelection(0);
                    }

                    etFiltroFechaDlg.setText(filtroCitas.getFechaS());
                    swFiltroCerradaDlg.setChecked(filtroCitas.isCerrada());
                    spFiltroModalidadPagoDlg.setSelection(mAdaptadorSpFiltroModalidadPago.getPosition(filtroCitas.getModalidadPago()));
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_filtro_citas, container, false);

        etFiltroFechaDlg = v.findViewById(R.id.etFiltroFechaDlg);
        swFiltroCerradaDlg = v.findViewById(R.id.swFiltroCerradaDlg);
        spFiltroModalidadPagoDlg = v.findViewById(R.id.spFiltroModalidadPagoDlg);
        spFiltroServicioDlg = v.findViewById(R.id.spFiltroServicioDlg);
        btFiltroFechaDlg = v.findViewById(R.id.btFiltroFechaDlg);
        btCancelar = v.findViewById(R.id.btCancelar);
        btAceptar = v.findViewById(R.id.btAceptar);
        pbFiltroCitas = v.findViewById(R.id.pbFiltroCitas);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        filtroCitas = coreVM.getFiltroCitas();
        coreVM.recuperarServicios();

        mAdaptadorSpFiltroModalidadPago = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, Pago.MODALIDAD_PAGO.values());
        spFiltroModalidadPagoDlg.setAdapter(mAdaptadorSpFiltroModalidadPago);
        swFiltroCerradaDlg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    swFiltroCerradaDlg.setText(getString(R.string.si));
                } else {
                    swFiltroCerradaDlg.setText(getString(R.string.no));
                }
            }
        });

        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coreVM.setMenuSeleccionado(R.id.menuItem_Citas);
                coreVM.getNavC().navigateUp();
            }
        });

        btAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm").withZone(DateTimeZone.forID("Europe/Madrid"));
                DateTime fechaSeleccionada = dateTimeFormatter.parseDateTime((String) Objects.requireNonNull(etFiltroFechaDlg.getText() + " 00:00"));
                filtroCitas.setFecha(fechaSeleccionada);

                if (spFiltroServicioDlg.getSelectedItemPosition() == 0) {
                    filtroCitas.setServicio(null);
                } else {
                    filtroCitas.setServicio((Servicio) spFiltroServicioDlg.getSelectedItem());
                }

                filtroCitas.setCerrada(swFiltroCerradaDlg.isChecked());
                filtroCitas.setModalidadPago((Pago.MODALIDAD_PAGO) spFiltroModalidadPagoDlg.getSelectedItem());

                coreVM.setMenuSeleccionado(R.id.menuItem_Citas);
                coreVM.getNavC().navigateUp();
            }
        });

        btFiltroFechaDlg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                Calendar fechaMinima = Calendar.getInstance();
                fechaMinima.set(mYear - 10, mMonth, mDay);

                Calendar fechaMaxima = Calendar.getInstance();
                fechaMaxima.set(Calendar.MONTH, mMonth);

                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(FiltroCitasFragment.this, fechaMaxima.get(Calendar.YEAR), fechaMaxima.get(Calendar.MONTH), fechaMaxima.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.setMinDate(fechaMinima);
                datePickerDialog.setMaxDate(fechaMaxima);
                datePickerDialog.setFirstDayOfWeek(2);
                datePickerDialog.show(getParentFragmentManager(), null);
            }
        });
    }

    //CallBack DatePickerDialog, setea la fecha
    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        etFiltroFechaDlg.setText(String.format(Locale.getDefault(), getString(R.string.fecha_Format), dayOfMonth, monthOfYear + 1, year));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        coreVM.setListadoServicios(null);
    }
}
