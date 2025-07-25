package mx.ssaj.surfingattendance;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
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
import mx.ssaj.surfingattendance.databinding.DialogSurfingnextConfigBinding;
import mx.ssaj.surfingattendance.detection.env.Logger;
import mx.ssaj.surfingattendance.surfingtime.SurfingTimeForegroundService;
import mx.ssaj.surfingattendance.surfingtime.services.SurfingTimeService;
import mx.ssaj.surfingattendance.surfingtime.services.SyncAttLogsService;
import mx.ssaj.surfingattendance.surfingtime.services.SyncInfoService;
import mx.ssaj.surfingattendance.surfingtime.services.SyncUsersService;
import mx.ssaj.surfingattendance.surfingtime.tasks.InfoTask;
import mx.ssaj.surfingattendance.surfingtime.tasks.SyncAttLogsTask;
import mx.ssaj.surfingattendance.surfingtime.tasks.SyncCommandsUpdatesTask;
import mx.ssaj.surfingattendance.surfingtime.tasks.SyncNewCommandsTask;
import mx.ssaj.surfingattendance.surfingtime.tasks.SyncUsersTask;
import mx.ssaj.surfingattendance.ui.facedetectionwrappers.SurfingDetectorActivityTest;
import mx.ssaj.surfingattendance.util.Util;

/**
 * TODO:
 * - Delete all references to TensorFlow, tfe, tfl2_logo.png, tfl2
 * - Search for those references in res/drawable res/layout
 *
 */
public class MainActivity extends AppCompatActivity {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "MainActivity";
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final String PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

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

        // Request permissions
        checkLocationPermission();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // Request permissions
        checkLocationPermission();
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
        if (item.getItemId() == R.id.action_face_recognition_test) {
            navigateToFaceRecognitionTestingActivity();
            return true;
        } else if (item.getItemId() == R.id.action_surfingtime_sync) {
            surfingTimeSync();
            return true;
        } else if (item.getItemId() == R.id.action_surfingnext_config) {
            surfingNextConfig();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void surfingNextConfig() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater =this.getLayoutInflater();
        DialogSurfingnextConfigBinding dialogSurfingnextConfigBinding = DialogSurfingnextConfigBinding.inflate(inflater);
        builder.setView(dialogSurfingnextConfigBinding.getRoot())
                // Add action buttons
                .setPositiveButton(R.string.dialog_surfingnext_otp_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String otp = dialogSurfingnextConfigBinding.terminalOtpTextOtp.getText().toString();
                        SurfingTimeService surfingTimeService = new SurfingTimeService(getApplicationContext());
                        SurfingAttendanceDatabase.databaseWriteExecutor.execute(() -> {
                            try {
                                surfingTimeService.configSurfingNext(otp);
                                runOnUiThread(() -> {
                                    Toast.makeText(getApplicationContext(), "SurfingNext configurado correctamente", Toast.LENGTH_LONG).show();
                                });
                            } catch (Exception e) {
                                runOnUiThread(() -> {
                                    Toast.makeText(getApplicationContext(), "Código OTP inválido o error de red", Toast.LENGTH_LONG).show();
                                });
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.dialog_surfingnext_otp_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        Dialog dialog = builder.create();
        dialog.show();
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

    private void checkLocationPermission() {
        if (checkSelfPermission(PERMISSION_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request location so that it gets cached
            if (fusedLocationProviderClient == null) {
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            }
            CurrentLocationRequest locationRequest = new CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setMaxUpdateAgeMillis(10 * 60 * 1000) // 10 min old location
                    .build();
            LOGGER.i(TAG, "Acquiring Location");
            fusedLocationProviderClient.getCurrentLocation(locationRequest, null).addOnSuccessListener(location -> {
                String geoLocation = null;
                if (location != null) {
                    geoLocation = String.format("%.6f,%.6f", location.getLongitude(), location.getLatitude());
                }
                LOGGER.i(TAG, "Location acquired %s", geoLocation);
            });
        } else {// Request permissions
            ActivityCompat.requestPermissions(this, new String[] {PERMISSION_LOCATION}, LOCATION_PERMISSION_REQUEST);
        }
    }
}