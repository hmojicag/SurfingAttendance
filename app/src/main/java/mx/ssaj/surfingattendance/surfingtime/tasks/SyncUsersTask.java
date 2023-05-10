package mx.ssaj.surfingattendance.surfingtime.tasks;

import android.app.Application;
import java.util.TimerTask;
import mx.ssaj.surfingattendance.detection.env.Logger;
import mx.ssaj.surfingattendance.surfingtime.services.SurfingTimeService;
import mx.ssaj.surfingattendance.surfingtime.services.SyncUsersService;

public class SyncUsersTask extends TimerTask {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "SyncUsersTask";
    private final SurfingTimeService surfingTimeService;
    private final SyncUsersService syncUsersService;
    private final Application application;

    public SyncUsersTask(SurfingTimeService surfingTimeService,
                         SyncUsersService syncUsersService,
                         Application application) {
        this.surfingTimeService = surfingTimeService;
        this.syncUsersService = syncUsersService;
        this.application = application;
    }


    @Override
    public void run() {
        if (!surfingTimeService.isEnabled()) {
            LOGGER.i(TAG, "SurfingTime Sync is not enabled");
            return;
        }

        try {
            // Sync Users, Profile Pictures and BioPhotos
            LOGGER.i(TAG, "SyncUsersTask is running...");
            syncUsersService.syncPendingUserInfo();
        } catch (Exception ex) {
            LOGGER.e(TAG, ex, "Error occurred while trying to sync Users and BioPhotos with SurfingTime");
        }
    }
}
