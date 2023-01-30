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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentInitBinding binding = FragmentInitBinding.inflate(inflater, container, false);
        getActivity().findViewById(R.id.main_bottomNavigationView).setVisibility(View.INVISIBLE);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (UsersModel.instance().isUserConnected()) {
            reloadUser(view);
            return;
        }
        Navigation.findNavController(view).navigate(
                InitFragmentDirections.actionInitFragmentToSignInFragment()
        );
    }

    private void reloadUser(View view) {
        UsersViewModel.instance().reloadUser(
                success -> Navigation.findNavController(view).navigate(
                        InitFragmentDirections.actionInitFragmentToPostsListFragment()
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
