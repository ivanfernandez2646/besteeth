package com.besteeth.vista.fragmentos;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.besteeth.R;
import com.besteeth.modelo.Cita;
import com.besteeth.modelo.FiltroCitas;
import com.besteeth.modelo.Pago;
import com.besteeth.vista.adaptadores.AdaptadorCitas;
import com.besteeth.vistamodelo.CoreViewModel;

import java.util.List;

/**
 * Clase CitasFragment
 *
 * Fragmento encargado de mostrar todas las
 * citas a partir de un filtro
 *
 * @author IVAN
 * @version 1.0
 */
public class CitasFragment extends Fragment {

    //Atributos
    private TextView tvFiltroFechaRes, tvFiltroServicioRes, tvFiltroCerradaRes, tvFiltroModalidadPagoRes, tvCitasErrorEstableciendoConexion, tvCitasSinResultados;
    private RecyclerView rvCitas;
    private AdaptadorCitas mAdaptadorCitas;
    private ProgressBar pbCitas;
    private SwipeRefreshLayout srCitas;

    private CoreViewModel coreVM;

    //Constructor
    public CitasFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
        }

        // Init Adaptador y ViewModel
        mAdaptadorCitas = new AdaptadorCitas();
        mAdaptadorCitas.setCitasFragment(true);
        mAdaptadorCitas.setOnLongClickListener(mostrarInfoCita_OnLongClickListener);
        coreVM = new ViewModelProvider(requireActivity()).get(CoreViewModel.class);

        //Observers
        coreVM.getCitas_listadoCitas().observe(this, new Observer<List<Cita>>() {
            @Override
            public void onChanged(List<Cita> citas) {
                if (citas != null) {
                    tvCitasErrorEstableciendoConexion.setVisibility(View.INVISIBLE);
                    if (citas.size() == 0) {
                        tvCitasSinResultados.setVisibility(View.VISIBLE);
                    } else {
                        tvCitasSinResultados.setVisibility(View.INVISIBLE);
                    }
                    mAdaptadorCitas.setDatos(citas);
                    mAdaptadorCitas.notifyDataSetChanged();
                    mAdaptadorCitas.setItemPos(-1);
                } else {
                    mAdaptadorCitas.setDatos(null);
                    mAdaptadorCitas.notifyDataSetChanged();
                    mAdaptadorCitas.setItemPos(-1);
                    tvCitasErrorEstableciendoConexion.setVisibility(View.VISIBLE);
                }
                pbCitas.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_filtro_citas, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_citas, container, false);

        tvFiltroFechaRes = v.findViewById(R.id.tvFiltroFechaRes);
        tvFiltroServicioRes = v.findViewById(R.id.tvFiltroServicioRes);
        tvFiltroCerradaRes = v.findViewById(R.id.tvFiltroCerradaRes);
        tvFiltroModalidadPagoRes = v.findViewById(R.id.tvFiltroModalidadPagoRes);
        tvCitasSinResultados = v.findViewById(R.id.tvCitasSinResultados);
        tvCitasErrorEstableciendoConexion = v.findViewById(R.id.tvCitasErrorEstableciendoConexion);
        rvCitas = v.findViewById(R.id.rvCitas);
        pbCitas = v.findViewById(R.id.pbCitas);
        srCitas = v.findViewById(R.id.srCitas);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FiltroCitas filtroCitas = coreVM.getFiltroCitas();
        tvFiltroFechaRes.setText(filtroCitas.getFechaS());
        tvFiltroServicioRes.setText(filtroCitas.getServicio() == null ? getString(R.string.no_Establecido) : filtroCitas.getServicio().getNombre());
        tvFiltroCerradaRes.setText(filtroCitas.isCerrada() ? getString(R.string.si) : getString(R.string.no));
        tvFiltroModalidadPagoRes.setText(filtroCitas.getModalidadPago().toString());

        if(filtroCitas.getModalidadPago() == Pago.MODALIDAD_PAGO.CUALQUIERA){
            tvFiltroModalidadPagoRes.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        }

        // Init RecyclerView Citas
        rvCitas.setHasFixedSize(true);
        rvCitas.setLayoutManager(new LinearLayoutManager(requireActivity()));
        rvCitas.addItemDecoration(new DividerItemDecoration(requireActivity(), DividerItemDecoration.HORIZONTAL));
        rvCitas.setAdapter(mAdaptadorCitas);

        pbCitas.setVisibility(View.VISIBLE);

        coreVM.recuperarCitas();

        //Listeners
        srCitas.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                coreVM.recuperarCitas();
                srCitas.setRefreshing(false);
                pbCitas.setVisibility(View.VISIBLE);
            }
        });
    }

    //Listener llamado desde el adaptador de las citas para mostrar información más detallada acerca
    //de las vistas
    private View.OnLongClickListener mostrarInfoCita_OnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Cita citaSeleccionada = mAdaptadorCitas.getDatos().get(mAdaptadorCitas.getItemPos());
            Bundle bundle = new Bundle();
            bundle.putParcelable("cita", citaSeleccionada);
            coreVM.getNavC().navigate(R.id.dlgInformacionCita, bundle);
            return true;
        }
    };

    @Override
    public void onDestroyView() {
        mAdaptadorCitas.setDatos(null);
        mAdaptadorCitas.setItemPos(-1);
        mAdaptadorCitas.notifyDataSetChanged();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
