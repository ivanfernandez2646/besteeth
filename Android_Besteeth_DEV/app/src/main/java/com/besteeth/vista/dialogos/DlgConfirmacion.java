package com.besteeth.vista.dialogos;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.besteeth.R;

/**
 * Clase DlgConfirmacion
 *
 * Diálogo general de confirmación reutilizado para diferentes mensajes
 *
 * @author IVAN
 * @version 1.0
 */
public class DlgConfirmacion extends DialogFragment {

    private int mId;

    public void setId(int id) {
        mId = id;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(null);
            if (getArguments() != null) {
                mId = getArguments().getInt("id");
                builder.setTitle(getArguments().getInt("titulo"));
                builder.setMessage(getArguments().getInt("mensaje"));
            }
            builder.setPositiveButton(R.string.bt_Aceptar, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mListener.onDlgConfirmacionPositiveClick(DlgConfirmacion.this, mId);
                }
            });
            builder.setNegativeButton(R.string.bt_Cancelar, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mListener.onDlgConfirmacionNegativeClick(DlgConfirmacion.this, mId);
                }
            });
            return builder.create();
        } else {
            return super.onCreateDialog(savedInstanceState);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public interface DlgConfirmacionListener {
        void onDlgConfirmacionPositiveClick(DialogFragment dialog, int mId);

        void onDlgConfirmacionNegativeClick(DialogFragment dialog, int mId);
    }

    private DlgConfirmacionListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (DlgConfirmacionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement DlgConfirmacionListener");
        }
    }

}
