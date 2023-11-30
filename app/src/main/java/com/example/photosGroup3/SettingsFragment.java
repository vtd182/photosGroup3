package com.example.photosGroup3;

import static android.content.Context.MODE_PRIVATE;

import static com.example.photosGroup3.AlbumsFragment.albumList;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    boolean isPasswordSet = false;
    String savedPass;
    String savedNumber;

    // TODO: Rename and change types of parameters

    LinearLayout privateAlbum;

    SharedPreferences sharePrf;
    SharedPreferences.Editor edit;

    SwitchCompat changeDark;

    public SettingsFragment() {
        // Required empty public constructor
    }


    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharePrf = requireContext().getSharedPreferences("AppPreferences", MODE_PRIVATE);
        edit = sharePrf.edit();
        isPasswordSet = sharePrf.getBoolean("pass_set", false);
        savedPass = sharePrf.getString("password","");
        savedNumber = sharePrf.getString("number_phone", "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        privateAlbum = view.findViewById(R.id.album_private);
        changeDark = view.findViewById(R.id.switchDarkmode);

        boolean status = ((MainActivity) requireContext()).getIsDark();
        changeDark.setChecked(status);
        changeDark.setOnCheckedChangeListener((compoundButton, isDark) -> {
            ((MainActivity) requireContext()).setIsDark(isDark);
        });

        privateAlbum.setOnClickListener(view1 -> {
            if (isPasswordSet){
                showInputPasswordDialog();
            } else {
                showSetPasswordDialog();
            }
        });

        return view;
    }

    private void showSetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.set_pass,null);
        Button cancelButton = dialogView.findViewById(R.id.set_pass_cancel_button);
        Button confirmButton = dialogView.findViewById(R.id.confirm_pass_button);
        EditText password = dialogView.findViewById(R.id.enter_set_pass);
        EditText confirmPassword = dialogView.findViewById(R.id.confirm_pass);
        EditText numberPhone = dialogView.findViewById(R.id.set_number_phone);

        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        cancelButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        confirmButton.setOnClickListener(v ->{
            String pass = password.getText().toString().trim();
            String cfpass = confirmPassword.getText().toString().trim();
            String num = numberPhone.getText().toString().trim();
            if (pass.length() < 5){
                Toast.makeText(getContext(),"Password must have more than 4 characters", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(cfpass)){
                Toast.makeText(getContext(),"Please enter the correct confirm-password", Toast.LENGTH_SHORT).show();
            } else if (num.length()!=10){
                Toast.makeText(getContext(),"Please enter the correct your number phone", Toast.LENGTH_SHORT).show();
            } else {
                edit.putBoolean("pass_set", true);
                edit.putString("password", pass);
                edit.putString("number_phone", num);
                edit.apply();
                alertDialog.dismiss();
                ((MainActivity) requireContext()).recreate();
                Toast.makeText(getContext(), "Set password success",Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void showInputPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.input_pass,null);
        Button okButton = dialogView.findViewById(R.id.ok_pass_button);
        EditText password = dialogView.findViewById(R.id.enter_input_pass);
        EditText numberPhone = dialogView.findViewById(R.id.input_number_phone);
        TextView forgotText = dialogView.findViewById(R.id.forgot_pass);

        forgotText.setOnClickListener(v -> {
            forgotText.setVisibility(View.INVISIBLE);
            dialogView.findViewById(R.id.forgot_layout).setVisibility(View.VISIBLE);
        });



        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        okButton.setOnClickListener(v->{
            String pass = password.getText().toString().trim();
            String num = numberPhone.getText().toString().trim();
            if (pass.equals(savedPass)){
                alertDialog.dismiss();
                showPrivateAlbum();
            } else if (num.equals(savedNumber)){
                alertDialog.dismiss();
                showPrivateAlbum();
            } else {
                if (dialogView.findViewById(R.id.forgot_layout).getVisibility() == View.INVISIBLE && !pass.equals(savedPass)){
                    Toast.makeText(getContext(), "Please enter the correct password",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Please enter the correct number phone",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showPrivateAlbum(){
        ((MainActivity) getContext()).getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, AlbumDisplayFragment.newInstance(albumList.get(0)), null)
                .setReorderingAllowed(true)
                .commit();
    }


}