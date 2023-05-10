package mx.ssaj.surfingattendanceapp.surfingtime;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import java.util.Timer;
import java.util.TimerTask;
import mx.ssaj.surfingattendanceapp.R;
import mx.ssaj.surfingattendanceapp.detection.env.Logger;
import mx.ssaj.surfingattendanceapp.surfingtime.services.SyncInfoService;
import mx.ssaj.surfingattendanceapp.surfingtime.services.SurfingTimeCommandExecutorService;
import mx.ssaj.surfingattendanceapp.surfingtime.services.SurfingTimeService;
import mx.ssaj.surfingattendanceapp.surfingtime.services.SyncAttLogsService;
import mx.ssaj.surfingattendanceapp.surfingtime.services.SyncUsersService;
import mx.ssaj.surfingattendanceapp.surfingtime.tasks.ExecuteCommandsTask;
import mx.ssaj.surfingattendanceapp.surfingtime.tasks.InfoTask;
import mx.ssaj.surfingattendanceapp.surfingtime.tasks.SyncAttLogsTask;
import mx.ssaj.surfingattendanceapp.surfingtime.tasks.SyncCommandsUpdatesTask;
import mx.ssaj.surfingattendanceapp.surfingtime.tasks.SyncNewCommandsTask;
import mx.ssaj.surfingattendanceapp.surfingtime.tasks.SyncUsersTask;

public class SurfingTimeForegroundService extends Service {
    private Timer timer;
    private SurfingTimeService surfingTimeService;
    private SyncInfoService syncInfoService;
    private SurfingTimeCommandExecutorService surfingTimeCommandExecutorService;
    private SyncAttLogsService syncAttLogsService;
    private SyncUsersService syncUsersService;
    private static final Logger LOGGER = new Logger();
    private static String TAG = "SurfingTimeForegroundService";

    // Sync INFO data every 10 minutes
    private static final long INFO_PERIOD = 10 * 60 * 1000;

    // Sync new Attendance records every 30 seconds
    private static final long SYNC_ATTENDANCE_RECORDS_PERIOD =  30 * 1000;

    // Sync changes in Users (like new BioPhotos) every 5 minute
    private static final long SYNC_USERS_PERIOD = 5 *  60 * 1000;

    // Try to pull new command from SurfingTime every 10 seconds (further delays are handled inside the task)
    public static final long SYNC_NEW_COMMANDS_PERIOD = 10 * 1000;

    // Sync pending command updates every 30 seconds
    private static final long SYNC_COMMAND_UPDATES_PERIOD =  30 * 1000;

    // Sync pending command updates every 10 seconds
    private static final long EXECUTE_COMMANDS_PERIOD =  10 * 1000;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOGGER.i("Starting Foreground Service");
        surfingTimeService = new SurfingTimeService(getApplicationContext());
        syncInfoService = new SyncInfoService(surfingTimeService, getApplication());
        syncAttLogsService = new SyncAttLogsService(surfingTimeService, getApplication());
        syncUsersService = new SyncUsersService(surfingTimeService, getApplication());
        surfingTimeCommandExecutorService = new SurfingTimeCommandExecutorService(surfingTimeService, syncInfoService, syncAttLogsService, syncUsersService, getApplication());
        timer = new Timer();
        TimerTask infoTask = new InfoTask(surfingTimeService, syncInfoService, getApplication());
        TimerTask syncAttLogsTask = new SyncAttLogsTask(surfingTimeService, syncAttLogsService, getApplication());
        TimerTask syncUsersTask = new SyncUsersTask(surfingTimeService, syncUsersService, getApplication());
        TimerTask syncNewCommandsTask = new SyncNewCommandsTask(surfingTimeService, getApplication());
        TimerTask syncCommandsUpdatesTask = new SyncCommandsUpdatesTask(surfingTimeService, getApplication());
        TimerTask executeCommandsTask = new ExecuteCommandsTask(surfingTimeService, surfingTimeCommandExecutorService, getApplication());
        timer.schedule(infoTask, 0, INFO_PERIOD);
        timer.schedule(syncAttLogsTask, 3000, SYNC_ATTENDANCE_RECORDS_PERIOD);
        timer.schedule(syncUsersTask, 3400, SYNC_USERS_PERIOD);
        timer.schedule(syncNewCommandsTask, 3800, SYNC_NEW_COMMANDS_PERIOD);
        timer.schedule(syncCommandsUpdatesTask, 4200, SYNC_COMMAND_UPDATES_PERIOD);
        timer.schedule(executeCommandsTask, 4600, EXECUTE_COMMANDS_PERIOD);

        // Create notification for Android status bar
        final String CHANNELID = "SurfingTimeForegroundService Service ID";
        NotificationChannel channel = new NotificationChannel(
                CHANNELID,
                CHANNELID,
                NotificationManager.IMPORTANCE_LOW
        );

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                .setContentText("Sync Service is running")
                .setContentTitle("SurfingTime Sync")
                .setSmallIcon(R.mipmap.ic_surfingattendance_foreground);

        startForeground(1001, notification.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
