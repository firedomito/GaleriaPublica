package com.example.galeriapublica;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GridViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GridViewFragment extends Fragment {

    public GridViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment GridViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GridViewFragment newInstance() {
        GridViewFragment fragment = new GridViewFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grid_view, container, false);
    }


    //Safepoint para acessar o mainActivity
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainActivityViewModel mainActivityViewModel = new ViewModelProvider(getActivity()).get(MainActivityViewModel.class);

        List<ImageData> imageDataList = new ArrayList<>(mainActivityViewModel.getImageDataList().values());

        GridAdapter gridAdapter = new GridAdapter(getContext(), imageDataList);

        //Tamanho na horizontal importado do util
        float w = getResources().getDimension(R.dimen.im_width);

        //calcular num de colunas
        int nColumns = Util.calculateNoOfColumns(getContext(), w);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), nColumns);

        RecyclerView rvGrid = getView().findViewById(R.id.rvGrid);
        rvGrid.setAdapter(gridAdapter);
        rvGrid.setLayoutManager(gridLayoutManager);


    }
}