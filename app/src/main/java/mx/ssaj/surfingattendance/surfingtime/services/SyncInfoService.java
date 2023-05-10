package mx.ssaj.surfingattendance.surfingtime.services;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.preference.PreferenceManager;

import java.util.Locale;
import mx.ssaj.surfingattendance.data.repositories.AttendanceRecordsRepository;
import mx.ssaj.surfingattendance.data.repositories.BioPhotosRepository;
import mx.ssaj.surfingattendance.data.repositories.UsersRepository;
import mx.ssaj.surfingattendance.detection.env.Logger;
import mx.ssaj.surfingattendance.surfingtime.dto.ApiInfoRequest;
import mx.ssaj.surfingattendance.surfingtime.dto.ApiInfoResponse;
import mx.ssaj.surfingattendance.util.Util;

public class SyncInfoService {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "InfoService";
    private final Application application;
    private final SurfingTimeService surfingTimeService;
    private final UsersRepository usersRepository;
    private final AttendanceRecordsRepository attendanceRecordsRepository;
    private final BioPhotosRepository bioPhotosRepository;

    public SyncInfoService(SurfingTimeService surfingTimeService, Application application) {
        this.application = application;
        this.surfingTimeService = surfingTimeService;
        usersRepository = new UsersRepository(application);
        attendanceRecordsRepository = new AttendanceRecordsRepository(application);
        bioPhotosRepository = new BioPhotosRepository(application);
    }

    public void syncInfo() throws Exception {
        String deviceName = Util.getDeviceNameForInfo(application);
        String modelName = Util.getDeviceModelForInfo();
        int usersCount = usersRepository.getUsersCount();
        int attRecordsCount = attendanceRecordsRepository.getAttendanceRecordCount();
        int bioPhotosCount = bioPhotosRepository.bioPhotosCount();
        String language = Locale.getDefault().getLanguage();
        String timeZone = Util.getTimezoneForInfo();
        String androidVersion = "Android " + Build.VERSION.RELEASE;
        String macAddress = Util.getMacAddress(application);
        long availableInternalMemory = Util.getAvailableInternalMemorySize();
        long totalInternalMemory = Util.getTotalInternalMemorySize();

        ApiInfoRequest apiInfoRequest = new ApiInfoRequest();
        apiInfoRequest.setDeviceName(deviceName);                       // Example: "Hazas moto g power (2022)"
        apiInfoRequest.setUsersCount(usersCount);
        apiInfoRequest.setAttLogsCount(attRecordsCount);
        apiInfoRequest.setFaceCount(bioPhotosCount);
        apiInfoRequest.setLanguage(language);                           // Example: "en"
        apiInfoRequest.setTimeZone(timeZone);                           // Example: "-6"
        apiInfoRequest.setModel("SurfingAttendance");
        apiInfoRequest.setPlatform(androidVersion);                     // Example: "Android 12"
        apiInfoRequest.setDeviceType(modelName);                        // Example: moto g power (2022)
        apiInfoRequest.setMacAddress(macAddress);                       // Example: "64:11:a4:b2:5f:c3"
        apiInfoRequest.setFreeFlashMemory(availableInternalMemory);     // Example: 34000000l
        apiInfoRequest.setFlashMemoryTotalAmount(totalInternalMemory);  // Example: 64000000l
        apiInfoRequest.setBioPhotoVerificationAvailable(true);
        apiInfoRequest.setFaceVerificationAvailable(false);
        apiInfoRequest.setVisibleFaceVerificationAvailable(false);
        apiInfoRequest.setFingerprintVerificationAvailable(false);

        // Sync info with SurfingTime
        ApiInfoResponse apiInfoResponse = surfingTimeService.info(apiInfoRequest);

        // Set settings into device
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("surfingTimeDelay", String.valueOf(apiInfoResponse.Delay));
        editor.commit();
    }

}
