package mx.ssaj.surfingattendance.ui.facedetectionwrappers.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import java.util.List;

import mx.ssaj.surfingattendance.data.model.BioPhotos;
import mx.ssaj.surfingattendance.data.repositories.BioPhotosRepository;

public class SurfingDetectorViewModel extends AndroidViewModel {
    private static String TAG = "SurfingDetectorViewModel";
    private BioPhotosRepository bioPhotosRepository;

    public SurfingDetectorViewModel(@NonNull Application application) {
        super(application);
        bioPhotosRepository = new BioPhotosRepository(application);
    }

    public List<BioPhotos> getFaceRegistry() {
        List<BioPhotos> bioPhotos = bioPhotosRepository.getFullAllBioPhotosForAttendance();
        return bioPhotos;
    }

}
