package com.example.secret;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.room.util.StringUtil;

import com.example.secret.databinding.FragmentSignInBinding;
import com.example.secret.interfaces.Listener;
import com.example.secret.model.UsersModel;
import com.example.secret.viewmodel.UsersViewModel;
import com.google.common.base.Strings;

public class SignInFragment extends Fragment {

    FragmentSignInBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().findViewById(R.id.main_bottomNavigationView).setVisibility(View.INVISIBLE);
        FragmentActivity parentActivity = getActivity();
        parentActivity.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.removeItem(R.id.signInFragment);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        },this, Lifecycle.State.RESUMED);
    }

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

        if (Strings.isNullOrEmpty(email) || Strings.isNullOrEmpty(password)){
            signInFailed.onComplete(null);
            return;
        }

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