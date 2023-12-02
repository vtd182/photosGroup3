package com.example.photosGroup3;

import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

public class SearchFragment extends Fragment implements ImageDisplay.LongClickCallback {

    static public SearchFragment getInstance() {
        if (instance == null) {
            instance = new SearchFragment();
        }
        return instance;
    }
    static SearchFragment instance=null;
    SearchView searchView;
    RecyclerView recyclerView;
    ListAdapter adapter;
    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Fragment fragment = getChildFragmentManager().findFragmentById(R.id.search_display_list);
                if (fragment instanceof ImageDisplay) {
                    ((ImageDisplay) fragment).searchImage(s);
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                Fragment fragment = getChildFragmentManager().findFragmentById(R.id.search_display_list);
                if (fragment instanceof ImageDisplay) {
                    ((ImageDisplay) fragment).searchImage(s);
                }
                return false;
            }
        });
        ImageDisplay.changeINSTANCE();
        ImageDisplay.getInstance().setLongClickCallBack(this);

        getChildFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.search_display_list, ImageDisplay.getInstance(), null)
                .commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.search_flagment, container, false);
        searchView = view.findViewById(R.id.searchView);
        return view;
    }

    @Override
    public void onLongClick() {

    }

    @Override
    public void afterLongClick() {

    }

    @Override
    public void onResume() {
        super.onResume();
        ImageDisplay.getInstance().getActionButton().setVisibility(View.GONE);
        ImageDisplay.getInstance().getToolbar().setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        ImageDisplay.restoreINSTANCE();
        super.onDestroyView();
    }
}