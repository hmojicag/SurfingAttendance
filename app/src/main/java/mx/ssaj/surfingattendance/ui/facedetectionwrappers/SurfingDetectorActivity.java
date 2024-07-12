package mx.ssaj.surfingattendance.ui.facedetectionwrappers;

import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.List;
import mx.ssaj.surfingattendance.data.SurfingAttendanceDatabase;
import mx.ssaj.surfingattendance.data.model.BioPhotos;
import mx.ssaj.surfingattendance.detection.DetectorActivity;
import mx.ssaj.surfingattendance.ui.facedetectionwrappers.viewmodels.SurfingDetectorViewModel;

public class SurfingDetectorActivity extends DetectorActivity {

    protected List<Integer> userIdsToSkip = new ArrayList<>();

    @Override
    protected List<BioPhotos> initializeFaceRegistry() {
        // Build Face Registry from DB
        SurfingDetectorViewModel surfingDetectorViewModel = new ViewModelProvider(this).get(SurfingDetectorViewModel.class);
        SurfingAttendanceDatabase.databaseWriteExecutor.execute(() -> {
            // Recover all faces from DB and register for detection
            List<BioPhotos> faceRegistry = surfingDetectorViewModel.getFaceRegistry();
            for(BioPhotos bioPhoto: faceRegistry) {
                if (!userIdsToSkip.contains(bioPhoto.user)) {
                    registerNewFace(bioPhoto);
                }
            }
        });

        // Return an empty array for now, while faces are fetched from DB
        return new ArrayList<>();
    }
}
