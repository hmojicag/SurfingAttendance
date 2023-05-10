package mx.ssaj.surfingattendanceapp.ui.users;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import mx.ssaj.surfingattendanceapp.R;
import mx.ssaj.surfingattendanceapp.databinding.FragmentUsersBinding;

public class UsersFragment extends Fragment {

    private FragmentUsersBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        UsersViewModel usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);
        binding = FragmentUsersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.buttonAddUser.setOnClickListener(view -> {
            mx.ssaj.surfingattendanceapp.ui.users.UsersFragmentDirections.ActionUsersFragmentToUsersUpsertFragment action =
                    UsersFragmentDirections.actionUsersFragmentToUsersUpsertFragment();
            // Passing -1 so that action is Add New
            action.setUserId(-1);
            Navigation.findNavController(view).navigate(action);
        });

        RecyclerView recyclerViewUsers = binding.recyclerViewUsers;
        final UserListItemAdapter userListItemAdapter = new UserListItemAdapter(new UserListItemAdapter.UsersDiff());
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewUsers.setAdapter(userListItemAdapter);

        usersViewModel.getAllUsersLive().observe(this.getViewLifecycleOwner(), users -> {
            userListItemAdapter.submitList(users);
        });

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}