package mx.ssaj.surfingattendance.ui.facedetectionwrappers;

import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.List;
import mx.ssaj.surfingattendance.data.SurfingAttendanceDatabase;
import mx.ssaj.surfingattendance.detection.DetectorActivity;
import mx.ssaj.surfingattendance.detection.dto.FaceRecord;
import mx.ssaj.surfingattendance.ui.facedetectionwrappers.viewmodels.SurfingDetectorViewModel;

public class SurfingDetectorActivity extends DetectorActivity {

    protected List<Integer> userIdsToSkip = new ArrayList<>();

    @Override
    protected List<FaceRecord> initializeFaceRegistry() {
        // Build Face Registry from DB
        SurfingDetectorViewModel surfingDetectorViewModel = new ViewModelProvider(this).get(SurfingDetectorViewModel.class);
        SurfingAttendanceDatabase.databaseWriteExecutor.execute(() -> {
            // Recover all faces from DB and register for detection
            List<FaceRecord> faceRegistry = surfingDetectorViewModel.getFaceRegistry();
            for(FaceRecord faceRecord: faceRegistry) {
                if (!userIdsToSkip.contains(faceRecord.getUserId())) {
                    registerNewFace(faceRecord);
                }
            }
        });

        // Return an empty array for now, while faces are fetched from DB
        return new ArrayList<>();
    }
}
