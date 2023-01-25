package com.example.secret;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.example.secret.databinding.FragmentSignInBinding;
import com.example.secret.interfaces.Listener;
import com.example.secret.model.UsersModel;
import com.example.secret.viewmodel.UsersViewModel;

public class SignInFragment extends Fragment {

    FragmentSignInBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        binding.signInProgressBar.setVisibility(View.INVISIBLE);

        NavDirections navToSignUp = SignInFragmentDirections.actionSignInFragmentToSignUpFragment();
        binding.registerBtn.setOnClickListener(Navigation.createNavigateOnClickListener(navToSignUp));

        binding.signinBtn.setOnClickListener(this::signIn);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (UsersModel.instance().isUserConnected()) {
            NavDirections navToAuthenticated = SignInFragmentDirections.actionSignInFragmentToUserSettingsFragment();
            setUser(view, navToAuthenticated);
        }
    }

    private void signIn(View view) {
        binding.signInProgressBar.setVisibility(View.VISIBLE);
        String email = binding.emailEt.getText().toString();
        String password = binding.passwordEt.getText().toString();

        NavDirections navToAuthenticated = SignInFragmentDirections.actionSignInFragmentToUserSettingsFragment();
        Listener<Void> signInSuccess = unused -> {
            setUser(view, navToAuthenticated);
            ;
        };
        Listener<Void> signInFailed = unused -> {
            binding.signInProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getActivity(), "Email or password are incorrect", Toast.LENGTH_SHORT).show();
        };

        UsersModel.instance().signIn(email, password, signInSuccess, signInFailed);
    }

    private void setUser(View view, NavDirections navToAuthenticated) {
        UsersViewModel.instance().setUser(
                success -> {
                    binding.signInProgressBar.setVisibility(View.INVISIBLE);
                    Navigation.findNavController(view).navigate(navToAuthenticated);
                },
                fail -> {
                    binding.signInProgressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(), "Could not get user data. authentication was successful", Toast.LENGTH_SHORT).show();
                });
    }

}