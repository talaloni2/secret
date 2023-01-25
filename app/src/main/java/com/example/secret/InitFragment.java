package com.example.secret;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.secret.databinding.FragmentInitBinding;
import com.example.secret.model.UsersModel;
import com.example.secret.viewmodel.UsersViewModel;

public class InitFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentInitBinding binding = FragmentInitBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (UsersModel.instance().isUserConnected()) {
            setUser(view);
            return;
        }
        Navigation.findNavController(view).navigate(
                InitFragmentDirections.actionInitFragmentToSignInFragment()
        );
    }

    private void setUser(View view) {
        UsersViewModel.instance().setUser(
                success -> Navigation.findNavController(view).navigate(
                        InitFragmentDirections.actionInitFragmentToUserSettingsFragment()
                ),
                fail -> {
                    UsersViewModel.instance().signOut();
                    Toast.makeText(getActivity(), "Sign In is required", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate(
                            InitFragmentDirections.actionInitFragmentToSignInFragment()
                    );
                });
    }
}