package mx.ssaj.surfingattendance.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import mx.ssaj.surfingattendance.databinding.FragmentHomeBinding;
import mx.ssaj.surfingattendance.ui.facedetectionwrappers.AttendanceRecordsByFaceDetectionActivity;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // OnClick @+id/button_open_attendance_face
        binding.buttonOpenAttendanceFace.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AttendanceRecordsByFaceDetectionActivity.class);
            startActivity(intent);
        });

        // OnClick @+id/button_open_attendance_password
        binding.buttonOpenAttendancePassword.setOnClickListener(view -> {
            // Go to Attendance by Password Full Screen Fragment
            NavDirections navDirection =
                    mx.ssaj.surfingattendance.ui.home.HomeFragmentDirections.actionHomeFragmentToAttendancePasswordFragment();
            Navigation.findNavController(view).navigate(navDirection);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}