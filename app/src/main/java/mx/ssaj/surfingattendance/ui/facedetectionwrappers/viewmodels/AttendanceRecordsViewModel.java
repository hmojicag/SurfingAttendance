package mx.ssaj.surfingattendance.ui.facedetectionwrappers.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import mx.ssaj.surfingattendance.data.model.AttendanceRecord;
import mx.ssaj.surfingattendance.data.model.Users;
import mx.ssaj.surfingattendance.data.repositories.AttendanceRecordsRepository;
import mx.ssaj.surfingattendance.data.repositories.UsersRepository;

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