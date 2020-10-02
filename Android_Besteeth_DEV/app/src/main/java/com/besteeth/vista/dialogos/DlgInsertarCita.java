package com.besteeth.vista.dialogos;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.besteeth.R;
import com.besteeth.modelo.Cita;
import com.besteeth.modelo.Configuracion;
import com.besteeth.modelo.Servicio;
import com.besteeth.vistamodelo.CoreViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.wdullaer.materialdatetimepicker.time.Timepoint;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Clase DlgInsertarCita
 *
 * Diálogo mostrado para insertar una cita,
 * ya sea mediante edición o una nueva alta
 *
 * @author IVAN
 * @version 1.0
 */
public class DlgInsertarCita extends DialogFragment implements TimePickerDialog.OnTimeSetListener, com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener {

    //Atributos
    private TextView tvInsertarCita;
    private EditText etFechaDlg, etHoraDlg;
    private Button btSeleccionarFechaDlg, btSeleccionarHoraDlg;
    private Spinner spServiciosDlg;
    private ProgressBar pbInsertarCita;

    //Utilizado si es un aplazamiento de cita
    private Cita citaAplazada;

    private CoreViewModel coreVM;
    private View v;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            v = inflater.inflate(R.layout.dialog_insertar_cita, null);
            if (getArguments() != null) {
                citaAplazada = getArguments().getParcelable("cita");
            }

            tvInsertarCita = v.findViewById(R.id.tvInsertarCita);
            etFechaDlg = v.findViewById(R.id.etFechaDlg);
            etHoraDlg = v.findViewById(R.id.etHoraDlg);
            btSeleccionarFechaDlg = v.findViewById(R.id.btSeleccionarFechaDlg);
            btSeleccionarHoraDlg = v.findViewById(R.id.btSeleccionarHoraDlg);
            spServiciosDlg = v.findViewById(R.id.spServiciosDlg);
            pbInsertarCita = v.findViewById(R.id.pbInsertarCita);

            builder.setView(v);
            builder.setPositiveButton(R.string.bt_Aceptar, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setNegativeButton(R.string.bt_Cancelar, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mListener.onDlgInsertarCitaNegativeClick(DlgInsertarCita.this);
                }
            });
            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Cita cita = new Cita();

                            if (coreVM.isAplazarCita()) {
                                cita = citaAplazada;
                                cita.setFechaHoraAntigua(cita.getFechaHoraUTC());
                            }

                            String resComprobarDatosObligatorios = comprobarDatosObligatorios();
                            if (resComprobarDatosObligatorios.equals(getString(R.string.msg_DatosObligatorios))) {
                                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm").withZone(DateTimeZone.forID("Europe/Madrid"));
                                DateTime fechaHoraSeleccionadas = dateTimeFormatter.parseDateTime((String) Objects.requireNonNull(etFechaDlg.getText() + " " + etHoraDlg.getText()));
                                cita.setFechaHora(fechaHoraSeleccionadas);
                                cita.setServicio((Servicio) spServiciosDlg.getSelectedItem());
                                cita.setPagoIniciado(false);
                                cita.setAtendida(false);
                                mListener.onDlgInsertarCitaPositiveClick(DlgInsertarCita.this, cita);
                                dialog.dismiss();
                            } else {
                                Snackbar.make(v, resComprobarDatosObligatorios, Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });
            return alertDialog;
        } else {
            return super.onCreateDialog(savedInstanceState);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        coreVM = new ViewModelProvider(requireActivity()).get(CoreViewModel.class);
        coreVM.getListadoServicios().observe(this, new Observer<List<Servicio>>() {
            @Override
            public void onChanged(List<Servicio> servicios) {
                pbInsertarCita.setVisibility(View.INVISIBLE);
                if (servicios != null) {
                    //Init Spinner
                    ArrayAdapter<Servicio> adapter = new ArrayAdapter<Servicio>(requireContext(), R.layout.support_simple_spinner_dropdown_item, servicios);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spServiciosDlg.setAdapter(adapter);
                }
            }
        });

        coreVM.getDlgInsertarCita_listadoHorasOcupadas().observe(this, new Observer<Timepoint[]>() {
            @Override
            public void onChanged(Timepoint[] timepoints) {
                pbInsertarCita.setVisibility(View.INVISIBLE);
                if (timepoints != null) {
                    // Get Current Hour
                    final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
                    int mHour = c.get(Calendar.HOUR_OF_DAY);
                    int mMinute = c.get(Calendar.MINUTE);

                    // Launch Time Picker Dialog
                    TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(DlgInsertarCita.this, mHour, mMinute, true);
                    timePickerDialog.enableSeconds(false);
                    timePickerDialog.setInitialSelection(mHour, mMinute);

                    if (timepoints.length > 0) {
                        timePickerDialog.setDisabledTimes(timepoints);
                    }

                    Configuracion configuracionVM = coreVM.getConfiguracion();
                    if (configuracionVM != null) {
                        timePickerDialog.setTimeInterval(1, configuracionVM.getIntervaloMinutosCita());
                        timePickerDialog.setMinTime(configuracionVM.getHoraApertura().getHourOfDay(), configuracionVM.getHoraApertura().getMinuteOfHour(), 0);
                        LocalTime horaCierreMenosMinutos = configuracionVM.getHoraCierre().minusMinutes(configuracionVM.getIntervaloMinutosCita());
                        timePickerDialog.setMaxTime(horaCierreMenosMinutos.getHourOfDay(), horaCierreMenosMinutos.getMinuteOfHour(), 0);
                    } else {

                        timePickerDialog.setTimeInterval(1, 5);
                    }
                    timePickerDialog.show(getParentFragmentManager(), null);
                } else {
                    Snackbar.make(v, getString(R.string.msg_ErrorConexionOdoo), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (coreVM.isAplazarCita()) {
            pbInsertarCita.setVisibility(View.INVISIBLE);
            tvInsertarCita.setText(getString(R.string.tv_InsertarCitaAplazar));

            DateTimeFormatter dtfFecha = DateTimeFormat.forPattern("dd/MM/yyyy").withZone(DateTimeZone.forID("Europe/Madrid"));
            String fechaActualS = dtfFecha.print(citaAplazada.getFechaHoraUTC().toInstant());
            etFechaDlg.setText(fechaActualS);

            DateTimeFormatter dftHora = DateTimeFormat.forPattern("HH:mm").withZone(DateTimeZone.forID("Europe/Madrid"));
            String horaActualS = dftHora.print(citaAplazada.getFechaHoraUTC().toInstant());
            etHoraDlg.setText(horaActualS);

            spServiciosDlg.setEnabled(false);
            List<Servicio> servicios = new ArrayList<>();
            servicios.add(citaAplazada.getServicio());
            ArrayAdapter<Servicio> adapter = new ArrayAdapter<>(requireContext(), R.layout.support_simple_spinner_dropdown_item, servicios);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spServiciosDlg.setAdapter(adapter);
            spServiciosDlg.setSelection(0);
        } else {
            tvInsertarCita.setText(getString(R.string.tv_InsertarCitaAlta));
            coreVM.recuperarServicios();
        }

        btSeleccionarFechaDlg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int mYear = 0;
                int mMonth = 0;
                int mDay = 0;

                if (coreVM.isAplazarCita()) {
                    mYear = citaAplazada.getFechaHoraUTC().getYear();
                    mMonth = citaAplazada.getFechaHoraUTC().getMonthOfYear() - 1;
                    mDay = citaAplazada.getFechaHoraUTC().getDayOfMonth();
                } else {
                    final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
                    mYear = c.get(Calendar.YEAR);
                    mMonth = c.get(Calendar.MONTH);
                    mDay = c.get(Calendar.DAY_OF_MONTH);
                }

                Calendar fechaMinima = Calendar.getInstance();
                if (coreVM.isAplazarCita()) {
                    fechaMinima.set(mYear, mMonth, mDay);
                } else {
                    fechaMinima.set(mYear, mMonth, mDay + 1);
                }

                Calendar fechaMinimaTMP = (Calendar) fechaMinima.clone();

                Calendar fechaMaxima = Calendar.getInstance();
                fechaMaxima.set(Calendar.MONTH, mMonth + 2);

                if(coreVM.isAplazarCita()){
                    fechaMaxima.set(Calendar.DAY_OF_MONTH, fechaMinimaTMP.get(Calendar.DAY_OF_MONTH));
                }

                Calendar fechaMaximaTMP = (Calendar) fechaMaxima.clone();

                Calendar primeraFechaValida = Calendar.getInstance();

                boolean primeraFecha = true;
                DatePickerDialog datePickerDialogTMP = DatePickerDialog.newInstance(DlgInsertarCita.this, 0, 0, 0);
                for (Calendar loopdate = fechaMinimaTMP; fechaMinimaTMP.before(fechaMaximaTMP); fechaMinimaTMP.add(Calendar.DATE, 1), loopdate = fechaMinimaTMP) {
                    int dayOfWeek = loopdate.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) {
                        Calendar[] disabledDays = new Calendar[1];
                        disabledDays[0] = loopdate;
                        datePickerDialogTMP.setDisabledDays(disabledDays);
                    } else if (primeraFecha) {
                        primeraFechaValida.set(loopdate.get(Calendar.YEAR), loopdate.get(Calendar.MONTH), loopdate.get(Calendar.DAY_OF_MONTH));
                        primeraFecha = false;
                    }
                }
                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(DlgInsertarCita.this, primeraFechaValida.get(Calendar.YEAR), primeraFechaValida.get(Calendar.MONTH), primeraFechaValida.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.setMinDate(fechaMinima);
                datePickerDialog.setMaxDate(fechaMaxima);
                datePickerDialog.setDisabledDays(datePickerDialogTMP.getDisabledDays());
                datePickerDialog.setFirstDayOfWeek(2);
                datePickerDialog.show(getParentFragmentManager(), null);
            }
        });


        btSeleccionarHoraDlg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etFechaDlg.getText().toString().equals(getString(R.string.et_FechaDlg))) {
                    pbInsertarCita.setVisibility(View.VISIBLE);
                    coreVM.recuperarHorasOcupadasDia(etFechaDlg.getText().toString());
                } else {
                    Snackbar.make(v, getString(R.string.msg_DebeSeleccionarFecha), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //CallBack de TimePickerDialog, setea la hora
    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        etHoraDlg.setText(String.format(Locale.getDefault(), getString(R.string.hora_Format), hourOfDay, minute));
    }

    //CallBack de DatePickerDialog, setea la fecha
    @Override
    public void onDateSet(com.wdullaer.materialdatetimepicker.date.DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        etFechaDlg.setText(String.format(Locale.getDefault(), getString(R.string.fecha_Format), dayOfMonth, monthOfYear + 1, year));
    }

    public interface DlgInsertarCitaListener {
        void onDlgInsertarCitaPositiveClick(DialogFragment dialog, Cita cita);

        void onDlgInsertarCitaNegativeClick(DialogFragment dialog);
    }


    private DlgInsertarCita.DlgInsertarCitaListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (DlgInsertarCita.DlgInsertarCitaListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DlgInsertarCitaListener");
        }
    }

    //Comprueba los datos obligatorios tras pulsar en el botón de Aceptar
    private String comprobarDatosObligatorios() {
        String res = getString(R.string.msg_DatosObligatorios);

        if (etFechaDlg.getText().toString().equals(getString(R.string.et_FechaDlg)) || etFechaDlg.getText().toString().equals("")) {
            res += " " + getString(R.string.msg_FechaDlg);
        }

        if (etHoraDlg.getText().toString().equals(getString(R.string.et_HoraDlg)) || etHoraDlg.getText().toString().equals("")) {
            res += " " + getString(R.string.msg_HoraDlg);
        }

        if (spServiciosDlg.getSelectedItem() != null) {
            if (((Servicio) spServiciosDlg.getSelectedItem()).isServicioPlaceHolder()) {
                res += " " + getString(R.string.msg_ServicioDlg);
            }
        } else {
            res += " " + getString(R.string.msg_ServicioDlg);
        }
        return res;
    }
}
