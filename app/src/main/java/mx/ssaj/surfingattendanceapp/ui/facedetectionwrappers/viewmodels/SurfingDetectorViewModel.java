package mx.ssaj.surfingattendanceapp.ui.facedetectionwrappers.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.util.ArrayList;
import java.util.List;

import mx.ssaj.surfingattendanceapp.data.model.BioPhotos;
import mx.ssaj.surfingattendanceapp.data.repositories.BioPhotosRepository;
import mx.ssaj.surfingattendanceapp.detection.dto.FaceRecord;
import mx.ssaj.surfingattendanceapp.detection.tflite.SimilarityClassifier;

public class SurfingDetectorViewModel extends AndroidViewModel {
    private static String TAG = "SurfingDetectorViewModel";
    private BioPhotosRepository bioPhotosRepository;

    public SurfingDetectorViewModel(@NonNull Application application) {
        super(application);
        bioPhotosRepository = new BioPhotosRepository(application);
    }

    public List<FaceRecord> getFaceRegistry() {
        List<BioPhotos> bioPhotos = bioPhotosRepository.getFullAllBioPhotosForAttendance();
        List<FaceRecord> faceRecords = new ArrayList<>();
        for (BioPhotos bioPhoto: bioPhotos) {
            try {// Build Face Record using BioPhoto and features
                FaceRecord faceRecord = new FaceRecord();
                faceRecord.setUserId(bioPhoto.user);
                SimilarityClassifier.Recognition recognition =
                        new SimilarityClassifier.Recognition("0", bioPhoto.getBioPhotoStringIdentifier(), 0f, null);
                recognition.setExtra(bioPhoto.Features.getFeature());
                recognition.setSurfingAttendanceUserId(bioPhoto.user);
                faceRecord.setRecognition(recognition);
                faceRecord.setName(bioPhoto.getBioPhotoStringIdentifier());
                faceRecord.setFaceFullPhoto(bioPhoto.getPhoto());
                faceRecord.setFaceThumbnail(bioPhoto.Thumbnail.getPhoto());
                faceRecords.add(faceRecord);
            } catch (Exception ex) {
                Log.e(TAG, "Error while fetching BioPhoto from DB and building FaceRecord. BioPhoto for " + bioPhoto.getBioPhotoStringIdentifier(), ex);
            }
        }
        return faceRecords;
    }

}
