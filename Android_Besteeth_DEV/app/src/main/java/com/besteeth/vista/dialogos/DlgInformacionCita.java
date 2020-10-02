package com.besteeth.vista.dialogos;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.besteeth.R;
import com.besteeth.modelo.Cita;
import com.besteeth.modelo.Pago;

import java.util.List;
import java.util.Locale;

/**
 * Clase DlgInformacionCita
 *
 * Diálogo mostrado tras un LongClick en una cita desde el fragmento CitasFragment.
 * Muestra toda la información relacionada con una cita
 *
 * @author IVAN
 * @version 1.0
 */
public class DlgInformacionCita extends DialogFragment {

    //Atributos
    private TextView tvFechaDlg, tvHoraDlg, tvServicioDlg, tvDoctorDlg, tvCerrada;
    private TableLayout tabPagos;
    private LinearLayout layoutNoPagos;
    private ImageView ivCerrada;

    private Cita cita;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View v = inflater.inflate(R.layout.dialog_informacion_cita, null);
            if (getArguments() != null) {
                cita = getArguments().getParcelable("cita");
            }

            tvFechaDlg = v.findViewById(R.id.tvFechaDlg);
            tvHoraDlg = v.findViewById(R.id.tvHoraDlg);
            tvServicioDlg = v.findViewById(R.id.tvServicioDlg);
            tvDoctorDlg = v.findViewById(R.id.tvDoctorDlg);
            tabPagos = v.findViewById(R.id.tabPagos);
            layoutNoPagos = v.findViewById(R.id.layoutNoPagos);
            tvCerrada = v.findViewById(R.id.tvCerrada);
            ivCerrada = v.findViewById(R.id.ivCerrada);

            builder.setView(v);
            builder.setPositiveButton(R.string.bt_Aceptar, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mListener.onDlgInformacionCitaPositiveClick(DlgInformacionCita.this);
                }
            });
            return builder.create();
        } else {
            return super.onCreateDialog(savedInstanceState);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (cita != null) {
            tvFechaDlg.setText(Html.fromHtml(String.format(Locale.getDefault(), "<b><big>" + getString(R.string.tv_FechaDlg) + "</big></b>&nbsp; %s", cita.getFechaS())));
            tvHoraDlg.setText(Html.fromHtml(String.format(Locale.getDefault(), "<b><big>" + getString(R.string.tv_HoraDlg) + "</big></b>&nbsp; %s", cita.getHoraS())));
            tvServicioDlg.setText(Html.fromHtml(String.format(Locale.getDefault(), "<b><big>" + getString(R.string.tv_ServicioDlg) + "</big></b>&nbsp; %s", cita.getServicio())));
            tvDoctorDlg.setText(Html.fromHtml(String.format(Locale.getDefault(), "<b><big>" + getString(R.string.tv_DoctorDlg) + "</big></b>&nbsp; %s", cita.getNameDoctor() == null
                    ? getString(R.string.tv_NoDoctor) : cita.getNameDoctor())));
            rellenarPagos(cita.getPagos());

            if (cita.isCerrada()) {
                tvCerrada.setText(getString(R.string.tv_CerradaOK));
                tvCerrada.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                ivCerrada.setColorFilter(getResources().getColor(android.R.color.holo_green_light));
            } else {
                tvCerrada.setText(getString(R.string.tv_CerradaKO));
                tvCerrada.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                ivCerrada.setColorFilter(getResources().getColor(android.R.color.holo_red_light));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public interface DlgInformacionCitaListener {
        void onDlgInformacionCitaPositiveClick(DialogFragment dialog);
    }

    private DlgInformacionCitaListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (DlgInformacionCitaListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DlgInformacionCitaListener");
        }
    }

    //Encargado de rellenar la tabla de los pagos
    private void rellenarPagos(List<Pago> pagos) {
        if (pagos.size() == 0) {
            tabPagos.setVisibility(View.GONE);
        } else {
            layoutNoPagos.setVisibility(View.GONE);
            Pago.MODALIDAD_PAGO modalidadPago = pagos.get(0).getModalidadPago();
            generarCabeceras(modalidadPago);

            if (modalidadPago == Pago.MODALIDAD_PAGO.CUOTAS) {
                for (int i = 0; i < pagos.size(); i++) {
                    Pago pago = pagos.get(i);
                    TableRow row = new TableRow(requireContext());

                    TextView tvFechaCuotas = new TextView(requireContext());
                    tvFechaCuotas.setGravity(Gravity.CENTER_HORIZONTAL);
                    tvFechaCuotas.setText(pago.getFechaS());
                    if (i % 2 != 0) {
                        tvFechaCuotas.setBackground(requireContext().getDrawable(R.drawable.rv_pagos_impar_border));
                    } else {
                        tvFechaCuotas.setBackground(requireContext().getDrawable(R.drawable.rv_pagos_par_border));
                    }
                    row.addView(tvFechaCuotas);

                    TextView tvCuotasCuota = new TextView(requireContext());
                    tvCuotasCuota.setGravity(Gravity.CENTER_HORIZONTAL);
                    tvCuotasCuota.setText(String.valueOf(pago.getCantidadCuotas()));
                    if (i % 2 != 0) {
                        tvCuotasCuota.setBackground(requireContext().getDrawable(R.drawable.rv_pagos_impar_border));
                    } else {
                        tvCuotasCuota.setBackground(requireContext().getDrawable(R.drawable.rv_pagos_par_border));
                    }
                    row.addView(tvCuotasCuota);

                    TextView tvPagadasCuota = new TextView(requireContext());
                    tvPagadasCuota.setGravity(Gravity.CENTER_HORIZONTAL);
                    tvPagadasCuota.setText(String.valueOf(pago.getCuotasPagadas()));
                    if (i % 2 != 0) {
                        tvPagadasCuota.setBackground(requireContext().getDrawable(R.drawable.rv_pagos_impar_border));
                    } else {
                        tvPagadasCuota.setBackground(requireContext().getDrawable(R.drawable.rv_pagos_par_border));
                    }
                    row.addView(tvPagadasCuota);

                    TextView tvRestantesCuota = new TextView(requireContext());
                    tvRestantesCuota.setGravity(Gravity.CENTER_HORIZONTAL);
                    tvRestantesCuota.setText(String.valueOf(pago.getCuotasRestantes()));
                    if (i % 2 != 0) {
                        tvRestantesCuota.setBackground(requireContext().getDrawable(R.drawable.rv_pagos_impar_border));
                    } else {
                        tvRestantesCuota.setBackground(requireContext().getDrawable(R.drawable.rv_pagos_par_border));
                    }
                    row.addView(tvRestantesCuota);

                    TextView tvPagoCuotaCuota = new TextView(requireContext());
                    tvPagoCuotaCuota.setGravity(Gravity.CENTER_HORIZONTAL);
                    tvPagoCuotaCuota.setText(String.format(Locale.getDefault(), "%.2f", pago.getPagoCuota()));
                    if (i % 2 != 0) {
                        tvPagoCuotaCuota.setBackground(requireContext().getDrawable(R.drawable.rv_pagos_impar_border));
                    } else {
                        tvPagoCuotaCuota.setBackground(requireContext().getDrawable(R.drawable.rv_pagos_par_border));
                    }
                    row.addView(tvPagoCuotaCuota);

                    tabPagos.addView(row);
                }

                Pago ultimoPago = pagos.get(pagos.size() - 1);
                rellenarSubtotalYTotal(ultimoPago.getCuotasPagadas() * ultimoPago.getPagoCuota(), ultimoPago.getPagoTotal(), ultimoPago.getModalidadPago(), pagos.size());
            } else {
                Pago pago = pagos.get(0);
                TableRow row = new TableRow(requireContext());

                TextView tvFechaUnica = new TextView(requireContext());
                tvFechaUnica.setGravity(Gravity.CENTER_HORIZONTAL);
                tvFechaUnica.setText(pago.getFechaS());
                tvFechaUnica.setBackground(requireContext().getDrawable(R.drawable.rv_pagos_par_border));
                row.addView(tvFechaUnica);

                TextView tvPagoUnica = new TextView(requireContext());
                tvPagoUnica.setGravity(Gravity.CENTER_HORIZONTAL);
                tvPagoUnica.setText(String.format(Locale.getDefault(), "%.2f", pago.getPagoTotal()));
                tvPagoUnica.setBackground(requireContext().getDrawable(R.drawable.rv_pagos_par_border));
                row.addView(tvPagoUnica);

                tabPagos.addView(row);
                rellenarSubtotalYTotal(pago.getPagoTotal(), pago.getPagoTotal(), pago.getModalidadPago(), 1);
            }
        }
    }

    //Genera las cabeceras de la tabla de los pagos
    private void generarCabeceras(Pago.MODALIDAD_PAGO modalidadPago) {
        switch (modalidadPago) {
            case PAGO_UNICO:
                TableRow rowUnica = new TableRow(requireContext());

                TextView tvFechaUnica = new TextView(requireContext());
                tvFechaUnica.setTypeface(null, Typeface.BOLD);
                tvFechaUnica.setGravity(Gravity.CENTER_HORIZONTAL);
                tvFechaUnica.setText(getString(R.string.tv_FechaTable));
                tvFechaUnica.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_impar_border));
                rowUnica.addView(tvFechaUnica);

                TextView tvPagoUnica = new TextView(requireContext());
                tvPagoUnica.setTypeface(null, Typeface.BOLD);
                tvPagoUnica.setGravity(Gravity.CENTER);
                tvPagoUnica.setText(getString(R.string.tv_PagoTable));
                tvPagoUnica.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_impar_border));
                rowUnica.addView(tvPagoUnica);

                tabPagos.addView(rowUnica);
                tabPagos.setStretchAllColumns(true);
                break;
            case CUOTAS:
                TableRow row = new TableRow(requireContext());

                TextView tvFechaCuota = new TextView(requireContext());
                tvFechaCuota.setTypeface(null, Typeface.BOLD);
                tvFechaCuota.setGravity(Gravity.CENTER);
                tvFechaCuota.setText(getString(R.string.tv_FechaTable));
                tvFechaCuota.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_impar_border));

                TextView tvCuotasCuota = new TextView(requireContext());
                tvCuotasCuota.setTypeface(null, Typeface.BOLD);
                tvCuotasCuota.setGravity(Gravity.CENTER);
                tvCuotasCuota.setText(getString(R.string.tv_CuotasTable));
                tvCuotasCuota.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_impar_border));

                TextView tvPagadasCuota = new TextView(requireContext());
                tvPagadasCuota.setTypeface(null, Typeface.BOLD);
                tvPagadasCuota.setGravity(Gravity.CENTER);
                tvPagadasCuota.setText(getString(R.string.tv_PagadasTable));
                tvPagadasCuota.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_impar_border));

                TextView tvRestantesCuota = new TextView(requireContext());
                tvRestantesCuota.setTypeface(null, Typeface.BOLD);
                tvRestantesCuota.setGravity(Gravity.CENTER);
                tvRestantesCuota.setText(getString(R.string.tv_RestantesTable));
                tvRestantesCuota.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_impar_border));

                TextView tvPagoCuotaCuota = new TextView(requireContext());
                tvPagoCuotaCuota.setTypeface(null, Typeface.BOLD);
                tvPagoCuotaCuota.setGravity(Gravity.CENTER);
                tvPagoCuotaCuota.setText(getString(R.string.tv_PagoTable));
                tvPagoCuotaCuota.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_impar_border));

                row.addView(tvFechaCuota);
                row.addView(tvCuotasCuota);
                row.addView(tvPagadasCuota);
                row.addView(tvRestantesCuota);
                row.addView(tvPagoCuotaCuota);
                tabPagos.addView(row);
                tabPagos.setStretchAllColumns(true);
                break;
        }
    }

    //Rellena la fila de subtotal y total en la tabla de los pagos
    private void rellenarSubtotalYTotal(double subtotal, double total, Pago.MODALIDAD_PAGO modalidadPago, int cantidadPagos) {
        if (modalidadPago == Pago.MODALIDAD_PAGO.CUOTAS) {
            TableRow rowSubtotal = new TableRow(requireContext());
            for (int i = 0; i < 3; i++) {
                TextView tvTransparent = new TextView(requireContext());
                tvTransparent.setBackgroundColor(Color.TRANSPARENT);
                rowSubtotal.addView(tvTransparent);
            }

            TextView tvSubtotalCuota = new TextView(requireContext());
            tvSubtotalCuota.setTypeface(null, Typeface.BOLD);
            tvSubtotalCuota.setGravity(Gravity.CENTER);
            tvSubtotalCuota.setText(getString(R.string.tv_SubtotalTable));
            if (cantidadPagos % 2 != 0) {
                tvSubtotalCuota.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_impar_border));
            } else {
                tvSubtotalCuota.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_par_border));
            }
            rowSubtotal.addView(tvSubtotalCuota);

            TextView tvSubtotalCuotaValor = new TextView(requireContext());
            tvSubtotalCuotaValor.setTypeface(null, Typeface.BOLD);
            tvSubtotalCuotaValor.setGravity(Gravity.CENTER);
            tvSubtotalCuotaValor.setText(String.format(Locale.getDefault(), "%.2f", subtotal));
            if (cantidadPagos % 2 != 0) {
                tvSubtotalCuotaValor.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_impar_border));
            } else {
                tvSubtotalCuotaValor.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_par_border));
            }
            rowSubtotal.addView(tvSubtotalCuotaValor);

            tabPagos.addView(rowSubtotal);

            TableRow rowTotal = new TableRow(requireContext());
            for (int i = 0; i < 3; i++) {
                TextView tvTransparent = new TextView(requireContext());
                tvTransparent.setBackgroundColor(Color.TRANSPARENT);
                rowTotal.addView(tvTransparent);
            }

            TextView tvTotalAPagarCuota = new TextView(requireContext());
            tvTotalAPagarCuota.setTypeface(null, Typeface.BOLD);
            tvTotalAPagarCuota.setGravity(Gravity.CENTER);
            tvTotalAPagarCuota.setText(getString(R.string.tv_TotalTable));
            if (cantidadPagos % 2 != 0) {
                tvTotalAPagarCuota.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_par_border));
            } else {
                tvTotalAPagarCuota.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_impar_border));
            }
            rowTotal.addView(tvTotalAPagarCuota);

            TextView tvTotalAPagarCuotaValor = new TextView(requireContext());
            tvTotalAPagarCuotaValor.setTypeface(null, Typeface.BOLD);
            tvTotalAPagarCuotaValor.setGravity(Gravity.CENTER);
            tvTotalAPagarCuotaValor.setText(String.valueOf(total));
            tvTotalAPagarCuotaValor.setText(String.format(Locale.getDefault(), "%.2f", total));
            if (cantidadPagos % 2 != 0) {
                tvTotalAPagarCuotaValor.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_par_border));
            } else {
                tvTotalAPagarCuotaValor.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_impar_border));
            }
            rowTotal.addView(tvTotalAPagarCuotaValor);

            tabPagos.addView(rowTotal);
        } else {
            TableRow rowTotal = new TableRow(requireContext());

            TextView tvTotalUnica = new TextView(requireContext());
            tvTotalUnica.setTypeface(null, Typeface.BOLD);
            tvTotalUnica.setGravity(Gravity.CENTER);
            tvTotalUnica.setText(getString(R.string.tv_TotalTable));
            tvTotalUnica.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_impar_border));
            rowTotal.addView(tvTotalUnica);

            TextView tvTotalUnicaValor = new TextView(requireContext());
            tvTotalUnicaValor.setTypeface(null, Typeface.BOLD);
            tvTotalUnicaValor.setGravity(Gravity.CENTER);
            tvTotalUnicaValor.setText(String.valueOf(total));
            tvTotalUnicaValor.setText(String.format(Locale.getDefault(), "%.2f", total));
            tvTotalUnicaValor.setBackground(requireActivity().getDrawable(R.drawable.rv_pagos_impar_border));
            rowTotal.addView(tvTotalUnicaValor);

            tabPagos.addView(rowTotal);
        }
    }
}

