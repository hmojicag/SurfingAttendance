package mx.ssaj.surfingattendanceapp.ui.facedetectionwrappers.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import mx.ssaj.surfingattendanceapp.data.model.AttendanceRecord;
import mx.ssaj.surfingattendanceapp.data.model.Users;
import mx.ssaj.surfingattendanceapp.data.repositories.AttendanceRecordsRepository;
import mx.ssaj.surfingattendanceapp.data.repositories.UsersRepository;

public class AttendanceRecordsViewModel extends AndroidViewModel {
    private static String TAG = "AttendanceRecordsViewModel";
    private AttendanceRecordsRepository attendanceRecordsRepository;
    private UsersRepository usersRepository;

    public AttendanceRecordsViewModel(@NonNull Application application) {
        super(application);
        attendanceRecordsRepository = new AttendanceRecordsRepository(application);
        usersRepository = new UsersRepository(application);
    }

    public Users findUserById(int userId) {
        return usersRepository.findFullById(userId);
    }

    public void persistAttendanceRecordWhilePunching(AttendanceRecord attendanceRecord) {
        attendanceRecordsRepository.persistAttendanceRecordWhilePunching(attendanceRecord);
    }
}