package mx.ssaj.surfingattendance;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;
import com.google.android.material.navigation.NavigationView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import mx.ssaj.surfingattendance.data.SurfingAttendanceDatabase;
import mx.ssaj.surfingattendance.databinding.ActivityMainBinding;
import mx.ssaj.surfingattendance.detection.env.Logger;
import mx.ssaj.surfingattendance.surfingtime.SurfingTimeForegroundService;
import mx.ssaj.surfingattendance.surfingtime.services.SurfingTimeService;
import mx.ssaj.surfingattendance.surfingtime.services.SyncAttLogsService;
import mx.ssaj.surfingattendance.surfingtime.services.SyncInfoService;
import mx.ssaj.surfingattendance.surfingtime.services.SyncUsersService;
import mx.ssaj.surfingattendance.surfingtime.tasks.ExecuteCommandsTask;
import mx.ssaj.surfingattendance.surfingtime.tasks.InfoTask;
import mx.ssaj.surfingattendance.surfingtime.tasks.SyncAttLogsTask;
import mx.ssaj.surfingattendance.surfingtime.tasks.SyncCommandsUpdatesTask;
import mx.ssaj.surfingattendance.surfingtime.tasks.SyncNewCommandsTask;
import mx.ssaj.surfingattendance.surfingtime.tasks.SyncUsersTask;
import mx.ssaj.surfingattendance.ui.facedetectionwrappers.SurfingDetectorActivityTest;
import mx.ssaj.surfingattendance.util.Util;

public class MainActivity extends AppCompatActivity {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_users, R.id.nav_attrecords, R.id.nav_settings)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Utils
        verifyDeviceSn();

        // Trigger Foreground Services
        startForeGroundServices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_face_recognition_test:
                navigateToFaceRecognitionTestingActivity();
                return true;
            case R.id.action_surfingtime_sync:
                surfingTimeSync();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // One time triggering of SurfingTime Synchronization of, in this order:
    // * Info
    // * Attlogs
    // * DeviceCommands Updates
    // * DeviceCommands New
    // * Users
    private void surfingTimeSync() {
        SurfingTimeService surfingTimeService = new SurfingTimeService(getApplicationContext());
        SyncInfoService syncInfoService = new SyncInfoService(surfingTimeService, getApplication());
        SyncAttLogsService syncAttLogsService = new SyncAttLogsService(surfingTimeService, getApplication());
        SyncUsersService syncUsersService = new SyncUsersService(surfingTimeService, getApplication());
        TimerTask infoTask = new InfoTask(surfingTimeService, syncInfoService, getApplication());
        TimerTask syncAttLogsTask = new SyncAttLogsTask(surfingTimeService, syncAttLogsService, getApplication());
        TimerTask syncUsersTask = new SyncUsersTask(surfingTimeService, syncUsersService, getApplication());
        TimerTask syncNewCommandsTask = new SyncNewCommandsTask(surfingTimeService, getApplication());
        TimerTask syncCommandsUpdatesTask = new SyncCommandsUpdatesTask(surfingTimeService, getApplication());

        // Run all services one after the other
        SurfingAttendanceDatabase.databaseWriteExecutor.execute(() -> {
            infoTask.run();
            syncAttLogsTask.run();
            syncUsersTask.run();
            syncNewCommandsTask.run();
            syncCommandsUpdatesTask.run();
        });
    }

    private void navigateToFaceRecognitionTestingActivity() {
        Intent intent = new Intent(getApplicationContext(), SurfingDetectorActivityTest.class);
        startActivity(intent);
    }

    private void startForeGroundServices() {
        // Services to run in ForeGround
        List<Class> foregroundServices = new ArrayList<>();

        // Verify if SurfingTime Foreground Service can be enabled
        if (SurfingTimeService.isAvailable(this)) {
            foregroundServices.add(SurfingTimeForegroundService.class);
        }

        for (Class serviceClass : foregroundServices) {
            if (!foregroundServiceRunning(serviceClass)) {
                Intent surfingTimeForeGroundServiceIntent = new Intent(this, SurfingTimeForegroundService.class);
                startForegroundService(surfingTimeForeGroundServiceIntent);
            }
        }
    }

    private boolean foregroundServiceRunning(Class serviceClass) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void verifyDeviceSn() {
        // Set the deviceSn with an autogenerated value
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String deviceSn = sharedPreferences.getString("deviceSn", "");
        if (StringUtils.isEmpty(deviceSn)) {
            LOGGER.i(TAG, "Generating new value for Device Serial Number");
            deviceSn = Util.generateNewDeviceSerialNumber();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("deviceSn", deviceSn);
            editor.commit();
        }
    }
}