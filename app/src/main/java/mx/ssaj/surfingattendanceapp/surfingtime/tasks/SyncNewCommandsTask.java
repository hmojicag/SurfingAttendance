package mx.ssaj.surfingattendanceapp.surfingtime.tasks;

import android.app.Application;
import org.apache.commons.collections4.CollectionUtils;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;
import mx.ssaj.surfingattendanceapp.data.model.SurfingTimeCommand;
import mx.ssaj.surfingattendanceapp.data.repositories.SurfingTimeCommandsRepository;
import mx.ssaj.surfingattendanceapp.detection.env.Logger;
import mx.ssaj.surfingattendanceapp.surfingtime.SurfingTimeForegroundService;
import mx.ssaj.surfingattendanceapp.surfingtime.services.SurfingTimeService;
import mx.ssaj.surfingattendanceapp.surfingtime.dto.ApiCommand;
import mx.ssaj.surfingattendanceapp.util.Util;

/**
 * Pull New Commands from SurfingTime
 */
public class SyncNewCommandsTask extends TimerTask {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "SyncNewCommandsTask";
    private final SurfingTimeService surfingTimeService;
    private final Application application;
    private final SurfingTimeCommandsRepository surfingTimeCommandsRepository;

    // Delays and Epoch in milliseconds
    private long epochLastTick;
    private final long maxDelay;
    private long delay;
    private long delayElapsed;

    public SyncNewCommandsTask(SurfingTimeService surfingTimeService, Application application) {
        this.surfingTimeService = surfingTimeService;
        this.application = application;
        surfingTimeCommandsRepository = new SurfingTimeCommandsRepository(application);
        maxDelay = Util.getDelaySetting(application) * 1000;
        delay = SurfingTimeForegroundService.SYNC_NEW_COMMANDS_PERIOD;
        delayElapsed = 0;
        epochLastTick = (new Date()).getTime();
    }

    @Override
    public void run() {
        if (!surfingTimeService.isEnabled()) {
            LOGGER.i(TAG, "SurfingTime Sync is not enabled");
            return;
        }

        try {
            LOGGER.i(TAG, "Syncing New Commands from SurfingTime");
            if (surfingTimeCommandsRepository.countPendingSync() > 0) {
                LOGGER.i(TAG, "There are commands pending to be synced, do not try to sync more commands or chances are we will receive same commands again");
                return;
            }

            Date now = new Date();
            delayElapsed += (now.getTime() - epochLastTick);
            epochLastTick = now.getTime();
            if (delayElapsed < delay) {
                LOGGER.i(TAG, "Desired delay not yet reached");
                return;
            }

            LOGGER.i(TAG, "Pulling New Commands from SurfingTime");
            // Get new commands from SurfingTime
            List<ApiCommand> apiCommands = surfingTimeService.getCommands();
            if (CollectionUtils.isEmpty(apiCommands)) {
                // No new commands?:
                // Increase delay up to max
                delay += SurfingTimeForegroundService.SYNC_NEW_COMMANDS_PERIOD;
                if (delay > maxDelay) {
                    delay = maxDelay;
                }

                // Return
                LOGGER.i(TAG, "No new commands in SurfingTime");
                return;
            }

            // OK we got new commands, persist commands on DB
            delayElapsed = 0;
            delay = SurfingTimeForegroundService.SYNC_NEW_COMMANDS_PERIOD;
            List<SurfingTimeCommand> surfingTimeCommands = apiCommands
                    .stream()
                    .map(apiCommand -> SurfingTimeCommand.fromApiCommand(apiCommand))
                    .collect(Collectors.toList());
            surfingTimeCommandsRepository.insertIgnore(surfingTimeCommands);
        } catch (Exception ex) {
            LOGGER.e(TAG, ex, "Error occurred while trying to sync new commands from SurfingTime");
        }
    }
}
