package mx.ssaj.surfingattendanceapp.ui.passwordattendance;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

import mx.ssaj.surfingattendanceapp.data.model.AttendanceRecord;
import mx.ssaj.surfingattendanceapp.data.model.Users;
import mx.ssaj.surfingattendanceapp.data.repositories.AttendanceRecordsRepository;
import mx.ssaj.surfingattendanceapp.data.repositories.UsersRepository;
import mx.ssaj.surfingattendanceapp.util.Literals;
import mx.ssaj.surfingattendanceapp.util.Util;
import mx.ssaj.surfingattendanceapp.util.VerifyType;

public class AttendanceByPasswordViewModel extends AndroidViewModel {
    private static String TAG = "AttendanceByPasswordViewModel";
    private AttendanceRecordsRepository attendanceRecordsRepository;
    private UsersRepository usersRepository;

    public AttendanceByPasswordViewModel(@NonNull Application application) {
        super(application);
        attendanceRecordsRepository = new AttendanceRecordsRepository(application);
        usersRepository = new UsersRepository(application);
    }

    public boolean validateUserPassword(Users user, String password) {
        if (user == null) {
            return false;
        }

        return StringUtils.equals(user.password, password);
    }

    public Users findUserById(int userId) {
        return usersRepository.findFullById(userId);
    }

    public void persistAttendanceRecordForUserNow(Users user) {
        Date now = new Date();
        AttendanceRecord attendanceRecord = new AttendanceRecord();
        attendanceRecord.user = user.user;
        attendanceRecord.verifyTime = Util.getFormatterDateTime(now);
        attendanceRecord.verifyTimeEpochMilliSeconds = now.getTime();
        attendanceRecord.verifyType = VerifyType.PASSWORD.getType();
        attendanceRecord.isSync = Literals.FALSE;
        attendanceRecordsRepository.insert(attendanceRecord);
    }

}