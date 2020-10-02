package com.besteeth.vista.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.besteeth.R;
import com.besteeth.modelo.Cita;

import java.util.List;

/**
 * Clase AdaptadorCitas
 *
 * Encargada de los items del RecyclerView tanto de próximas citas (InicioFragment)
 * como de las citas filtradas de (CitasFragment)
 *
 * @author IVAN
 * @version 1.0
 */
public class AdaptadorCitas extends RecyclerView.Adapter<AdaptadorCitas.CitaVH>{

    //Atributos
    private List<Cita> mDatos;
    private int mItemPos;
    private View.OnLongClickListener mListener;

    //Nos ayuda a saber si estamos en el fragmento CitasFragment
    //o en el de InicioFragment y así establecer un listener o no
    private boolean isCitasFragment;

    //Constructor
    public AdaptadorCitas() {
        this.mItemPos = -1;
    }

    //Getters and setters
    public void setDatos(List<Cita> mDatos) {
        this.mDatos = mDatos;
    }

    public List<Cita> getDatos() {
        return mDatos;
    }

    public int getItemPos() {
        return mItemPos;
    }

    public void setItemPos(int mItemPos) {
        this.mItemPos = mItemPos;
    }

    public void setOnLongClickListener(View.OnLongClickListener mListener) {
        this.mListener = mListener;
    }

    public boolean isCitasFragment() {
        return isCitasFragment;
    }

    public void setCitasFragment(boolean citasFragment) {
        isCitasFragment = citasFragment;
    }

    //Métodos
    @NonNull
    @Override
    public CitaVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_rv_citas, parent, false);
        return new CitaVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaVH holder, int position) {
        if(mDatos != null){
            holder.setItem(mDatos.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if(mDatos != null){
            return mDatos.size();
        }
        return 0;
    }

    //Clase ViewHolder para cada elemento
    public class CitaVH extends RecyclerView.ViewHolder {

        private TextView tvFecha, tvHora, tvServicio;

        public CitaVH(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvHora = itemView.findViewById(R.id.tvHora);
            tvServicio = itemView.findViewById(R.id.tvServicio);

            //Aquí comprobamos si estamos en un fragmento o en otro y ya seteamos
            // un LongClickListener o no
            if(isCitasFragment){
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int pos = getLayoutPosition();
                        notifyItemChanged(mItemPos);
                        mItemPos = pos;
                        notifyItemChanged(mItemPos);
                        if (mListener != null)
                            mListener.onLongClick(v);
                        return true;
                    }
                });
            }
        }

        private void setItem(Cita cita){
            tvFecha.setText(cita.getFechaS());
            tvHora.setText(cita.getHoraS());
            tvServicio.setText(cita.getServicio().toString());
        }
    }
}
