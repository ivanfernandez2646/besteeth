package com.besteeth.vista.fragmentos;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.besteeth.R;
import com.besteeth.modelo.Cita;
import com.besteeth.modelo.Cliente;
import com.besteeth.vista.adaptadores.AdaptadorCitas;
import com.besteeth.vistamodelo.CoreViewModel;
import com.google.android.material.snackbar.Snackbar;

import org.joda.time.DateTimeComparator;

import java.util.List;
import java.util.Locale;

/**
 * Clase InicioFragment
 * <p>
 * Fragmento encargado de mostrar info acerca del cliente
 * logueado. Además muestra las próximas citas a partir de la fecha y hora
 * actual, además de permitir la edición y el borrado de las mismas
 *
 * @author IVAN
 * @version 1.0
 */
public class InicioFragment extends Fragment {

    public static final int DIALOG_CONFIRMAR_BORRADO_CITA = 3;

    //Atributos
    private TextView tvNombreCliente, tvDniCliente, tvGastosTotalesCant, tvInicioSinResultados, tvInicioErrorEstableciendoConexion;
    private RecyclerView rvProximasCitas;
    private AdaptadorCitas mAdaptadorProximasCitas;
    private ProgressBar pbInicio;
    private SwipeRefreshLayout srInicio;

    private CoreViewModel coreVM;

    //Constructor
    public InicioFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        // Init Adaptador y ViewModel
        mAdaptadorProximasCitas = new AdaptadorCitas();
        coreVM = new ViewModelProvider(requireActivity()).get(CoreViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_inicio, container, false);

        tvNombreCliente = v.findViewById(R.id.tvNombreCliente);
        tvDniCliente = v.findViewById(R.id.tvDniCliente);
        tvGastosTotalesCant = v.findViewById(R.id.tvGastosTotalesCant);
        tvInicioSinResultados = v.findViewById(R.id.tvInicioSinResultados);
        tvInicioErrorEstableciendoConexion = v.findViewById(R.id.tvInicioErrorEstableciendoConexion);
        rvProximasCitas = v.findViewById(R.id.rvProximasCitas);
        pbInicio = v.findViewById(R.id.pbInicio);
        srInicio = v.findViewById(R.id.srInicio);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean[] pbNoMostrar = new boolean[2];

        //Inits
        coreVM.getCliente().observe(this, new Observer<Cliente>() {
            @Override
            public void onChanged(Cliente cliente) {
                if (cliente != null) {
                    tvNombreCliente.setText(cliente.getNombre());
                    tvDniCliente.setText(cliente.getDni());
                    tvGastosTotalesCant.setText(String.format(Locale.getDefault(), "%.2f€", cliente.getGastosTotales()));
                }
                pbNoMostrar[0] = true;
                ocultarProgressBar(pbNoMostrar);
            }
        });

        coreVM.getInicio_listadoProximasCitas().observe(this, new Observer<List<Cita>>() {
            @Override
            public void onChanged(List<Cita> citas) {
                if (citas != null) {
                    tvInicioErrorEstableciendoConexion.setVisibility(View.INVISIBLE);
                    if (citas.size() == 0) {
                        tvInicioSinResultados.setVisibility(View.VISIBLE);
                    } else {
                        tvInicioSinResultados.setVisibility(View.INVISIBLE);
                    }
                    mAdaptadorProximasCitas.setDatos(citas);
                    mAdaptadorProximasCitas.notifyDataSetChanged();
                    mAdaptadorProximasCitas.setItemPos(-1);
                } else {
                    mAdaptadorProximasCitas.setDatos(null);
                    mAdaptadorProximasCitas.notifyDataSetChanged();
                    mAdaptadorProximasCitas.setItemPos(-1);
                    tvInicioErrorEstableciendoConexion.setVisibility(View.VISIBLE);
                }
                pbNoMostrar[1] = true;
                ocultarProgressBar(pbNoMostrar);
            }
        });

        coreVM.getInicio_altaCita().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean != null) {
                    recargarRecyclerViewProximasCitas();
                    if (aBoolean) {
                        Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.dlg_AltaCitaOK, Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.dlg_AltaCitaKO, Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });

        coreVM.getInicio_bajaCita().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean != null) {
                    recargarRecyclerViewProximasCitas();
                    if (aBoolean) {
                        Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.dlg_BajaCitaOK, Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.dlg_BajaCitaKO, Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });

        coreVM.getInicio_aplazarCita().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean != null) {
                    recargarRecyclerViewProximasCitas();
                    if (aBoolean) {
                        Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.dlg_AplazarCita1DiaOK, Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.dlg_AplazarCita1DiaKO, Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });

        coreVM.getInicio_progressBar().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean != null) {
                    if (aBoolean) {
                        pbInicio.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        // Init RecyclerView ProximasCitas
        rvProximasCitas.setHasFixedSize(true);
        rvProximasCitas.setLayoutManager(new LinearLayoutManager(requireActivity()));
        rvProximasCitas.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.HORIZONTAL));
        rvProximasCitas.setAdapter(mAdaptadorProximasCitas);
        coreVM.setInicio_adaptadorProximasCitas(mAdaptadorProximasCitas);

        pbInicio.setVisibility(View.VISIBLE);

        ItemTouchHelper ith = new ItemTouchHelper(ithSimpleCallback);
        ith.attachToRecyclerView(rvProximasCitas);
        rvProximasCitas.setAdapter(mAdaptadorProximasCitas);

        coreVM.recuperarProximasCitas();
        coreVM.recuperarInfoCliente();

        //Listeners
        srInicio.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                coreVM.recuperarProximasCitas();
                coreVM.recuperarInfoCliente();
                srInicio.setRefreshing(false);
                pbInicio.setVisibility(View.VISIBLE);
            }
        });
    }

    private void ocultarProgressBar(boolean[] pbNoMostrar) {
        boolean pbNoMostrarKO = false;

        for (boolean b : pbNoMostrar) {
            if (!b) {
                pbNoMostrarKO = true;
            }
        }

        if (!pbNoMostrarKO) {
            pbInicio.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mAdaptadorProximasCitas.setDatos(null);
        mAdaptadorProximasCitas.setItemPos(-1);
        mAdaptadorProximasCitas.notifyDataSetChanged();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //Recarga el recycler view de las citas, así se evita duplicidad
    //de código
    private void recargarRecyclerViewProximasCitas() {
        mAdaptadorProximasCitas.setDatos(null);
        mAdaptadorProximasCitas.setItemPos(-1);
        mAdaptadorProximasCitas.notifyDataSetChanged();
        coreVM.recuperarProximasCitas();
    }

    //ItemTouchHelper para cada elemento del RecyclerView
    private ItemTouchHelper.SimpleCallback ithSimpleCallback =
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    mAdaptadorProximasCitas.setItemPos(viewHolder.getAdapterPosition());
                    Cita cita = mAdaptadorProximasCitas.getDatos().get(mAdaptadorProximasCitas.getItemPos());
                    if (direction == ItemTouchHelper.LEFT) { // eliminar
                        Bundle bundleDlgConfirmacion = new Bundle();
                        bundleDlgConfirmacion.putInt("id", DIALOG_CONFIRMAR_BORRADO_CITA);
                        bundleDlgConfirmacion.putInt("titulo", R.string.app_name);
                        bundleDlgConfirmacion.putInt("mensaje", R.string.msg_ConfirmacionBajaCita);
                        bundleDlgConfirmacion.putParcelable("cita", cita);
                        bundleDlgConfirmacion.putInt("posicion", mAdaptadorProximasCitas.getItemPos());
                        coreVM.getNavC().navigate(R.id.dlgConfirmacion, bundleDlgConfirmacion);
                    } else if (direction == ItemTouchHelper.RIGHT) { // aplazar cita
                        DateTimeComparator dateTimeComparator = DateTimeComparator.getDateOnlyInstance();
                        if (dateTimeComparator.compare(null, cita.getFechaHoraUTC().toDate()) == 0) {
                            Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.msg_ErrorAplazarCitaMismoDia, Snackbar.LENGTH_LONG).show();
                            mAdaptadorProximasCitas.notifyItemChanged(viewHolder.getAdapterPosition());
                        } else {
                            coreVM.setAplazarCita(true);
                            Bundle bundleDlgConfirmacion = new Bundle();
                            bundleDlgConfirmacion.putParcelable("cita", cita);
                            bundleDlgConfirmacion.putInt("posicion", mAdaptadorProximasCitas.getItemPos());
                            coreVM.getNavC().navigate(R.id.dlgInsertarCita, bundleDlgConfirmacion);
                        }
                    }
                    viewHolder.itemView.callOnClick();
                }

                @Override
                public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isCurrentlyActive) {
                        View itemView = viewHolder.itemView;
                        float height = (float) itemView.getBottom() - (float) itemView.getTop();
                        float width = height / 3;
                        Paint p = new Paint();
                        p.setColor(getResources().getColor(R.color.colorPrimary));
                        if (dX > 0) { // editar
                            RectF background = new RectF((float) itemView.getLeft() + 5, (float) itemView.getTop() + 10, dX - 5, (float) itemView.getBottom() - 10);
                            c.drawRect(background, p);
                            Bitmap icon = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_today);
                            RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                            c.drawBitmap(icon, null, icon_dest, p);
                        } else if (dX < 0) { // eliminar
                            RectF background = new RectF((float) itemView.getRight() + dX + 5, (float) itemView.getTop() + 10, (float) itemView.getRight() - 5, (float) itemView.getBottom() - 10);
                            p.setColor(getResources().getColor(R.color.colorPrimary));
                            c.drawRect(background, p);
                            Bitmap icon = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_delete);
                            RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                            c.drawBitmap(icon, null, icon_dest, p);
                        }
                    }
                }
            };
}
