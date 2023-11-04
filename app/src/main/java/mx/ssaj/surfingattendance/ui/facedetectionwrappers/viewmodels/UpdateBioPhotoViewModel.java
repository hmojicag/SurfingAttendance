package mx.ssaj.surfingattendance.ui.facedetectionwrappers.viewmodels;

import android.app.Application;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import org.apache.commons.lang3.ArrayUtils;
import java.util.ArrayList;
import java.util.List;
import mx.ssaj.surfingattendance.data.model.BioPhotoFeatures;
import mx.ssaj.surfingattendance.data.model.BioPhotos;
import mx.ssaj.surfingattendance.data.model.Users;
import mx.ssaj.surfingattendance.data.repositories.BioPhotoFeaturesRepository;
import mx.ssaj.surfingattendance.data.repositories.BioPhotosRepository;
import mx.ssaj.surfingattendance.data.repositories.UsersRepository;
import mx.ssaj.surfingattendance.detection.dto.FaceRecord;
import mx.ssaj.surfingattendance.detection.env.Logger;
import mx.ssaj.surfingattendance.util.BioDataType;
import mx.ssaj.surfingattendance.util.Literals;
import mx.ssaj.surfingattendance.util.Util;

public class UpdateBioPhotoViewModel extends AndroidViewModel {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "UpdateBioPhotoViewModel";
    private BioPhotosRepository bioPhotosRepository;
    private BioPhotoFeaturesRepository bioPhotoFeaturesRepository;
    private UsersRepository usersRepository;

    public UpdateBioPhotoViewModel(@NonNull Application application) {
        super(application);
        bioPhotosRepository = new BioPhotosRepository(application);
        bioPhotoFeaturesRepository = new BioPhotoFeaturesRepository(application);
        usersRepository = new UsersRepository(application);
    }

    public void upsertBioPhotos(int userId, Bitmap fullPhoto, FaceRecord face) {
        List<BioPhotos> bioPhotos = new ArrayList<>();

        // Full Photo to be stored in DB for SurfingAttendance and Horus
        BioPhotos biophoto = new BioPhotos();
        biophoto.user = userId;
        biophoto.type = BioDataType.BIOPHOTO_JPG.getType();
        biophoto.setBioPhotoContent(fullPhoto);
        biophoto.lastUpdated = Util.getDateTimeNow();
        biophoto.isSync = Literals.FALSE;
        bioPhotos.add(biophoto);

        // Thumbnail Photo to be stored in DB for SurfingAttendance
        BioPhotos thumbNailBiophoto = new BioPhotos();
        thumbNailBiophoto.user = userId;
        thumbNailBiophoto.type = BioDataType.BIOPHOTO_THUMBNAIL_JPG.getType();
        thumbNailBiophoto.setBioPhotoContent(face.getRecognition().getCrop());
        thumbNailBiophoto.lastUpdated = Util.getDateTimeNow();
        thumbNailBiophoto.isSync = Literals.FALSE;
        bioPhotos.add(thumbNailBiophoto);

        bioPhotosRepository.upsertBioPhotos(bioPhotos);

        // Store the face features of the fullPhoto in DB
        BioPhotoFeatures bioPhotoFeatures = new BioPhotoFeatures();
        bioPhotoFeatures.user = userId;
        bioPhotoFeatures.type = biophoto.type;
        bioPhotoFeatures.setFeatures((float[][]) face.getRecognition().getExtra());
        bioPhotoFeaturesRepository.upsert(bioPhotoFeatures);

        // Store a resized version of the full photo as new profile picture
        Users user = usersRepository.findFullById(userId);
        if (user != null) {
            // Scale down the size of the image
            Bitmap profilePicture = Util.getResizedBitmap(fullPhoto, 120, 120);
            user.setBioPhotoContent(profilePicture);
            user.isSync = Literals.FALSE;
            usersRepository.update(user);
        }

        // Log extras for debugging purposes
        LOGGER.i(TAG, "Logging extras while storing user" + userId + " to DB: " + ArrayUtils.toString(face.getRecognition().getExtra()));
    }

}
