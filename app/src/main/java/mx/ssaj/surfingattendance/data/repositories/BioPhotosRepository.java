package mx.ssaj.surfingattendance.data.repositories;

import android.app.Application;
import java.util.Arrays;
import java.util.List;
import mx.ssaj.surfingattendance.data.SurfingAttendanceDatabase;
import mx.ssaj.surfingattendance.data.dao.BioPhotoFeaturesDao;
import mx.ssaj.surfingattendance.data.dao.BioPhotosDao;
import mx.ssaj.surfingattendance.data.dao.UsersDao;
import mx.ssaj.surfingattendance.data.model.BioPhotos;
import mx.ssaj.surfingattendance.util.BioDataType;

public class BioPhotosRepository {

    private BioPhotosDao bioPhotosDao;
    private UsersDao usersDao;
    private BioPhotoFeaturesDao bioPhotoFeaturesDao;

    public BioPhotosRepository(Application application) {
        SurfingAttendanceDatabase surfingAttendanceDatabase = SurfingAttendanceDatabase.getDatabase(application);
        bioPhotosDao = surfingAttendanceDatabase.bioPhotosDao();
        usersDao = surfingAttendanceDatabase.usersDao();
        bioPhotoFeaturesDao = surfingAttendanceDatabase.bioPhotoFeaturesDao();
    }

    public int bioPhotosCount() {
        return bioPhotosDao.bioPhotosCount();
    }

    // We only Sync BioPhotos type 9 to SurfingTime
    public List<BioPhotos> getAllBioPhotosPendingSync() {
        return bioPhotosDao.getAllBioPhotosPendingSync();
    }

    public void markBioPhotoAsSynced(BioPhotos bioPhoto) {
        bioPhoto.isSync = 1;
        bioPhotosDao.update(bioPhoto);
    }

    public List<BioPhotos> getAllBioPhotosForAttendance() {
        return bioPhotosDao.getAllBioPhotosForAttendance();
    }

    public List<BioPhotos> getFullAllBioPhotosForAttendance() {
        List<BioPhotos> bioPhotos = bioPhotosDao.getAllBioPhotosForAttendance();
        for(BioPhotos bioPhoto: bioPhotos) {
            bioPhoto.User = usersDao.findById(bioPhoto.user);
            bioPhoto.Features = bioPhotoFeaturesDao.findById(bioPhoto.user, bioPhoto.type);
            bioPhoto.Thumbnail = bioPhotosDao.findById(bioPhoto.user, BioDataType.BIOPHOTO_THUMBNAIL_JPG.getType());
        }
        return bioPhotos;
    }

    public void upsertBioPhoto(BioPhotos bioPhoto) {
        upsertBioPhotos(Arrays.asList(bioPhoto));
    }

    public void upsertBioPhotos(List<BioPhotos> bioPhotos) {
        for(BioPhotos bioPhoto: bioPhotos) {
            BioPhotos bioPhotoBd = bioPhotosDao.findById(bioPhoto.user, bioPhoto.type);
            if (bioPhotoBd == null) {// Insert new
                bioPhotosDao.insert(bioPhoto);
            } else {// Set editable fields and update record
                bioPhotoBd.photoIdName = bioPhoto.photoIdName;
                bioPhotoBd.photoIdSize = bioPhoto.photoIdSize;
                bioPhotoBd.photoIdContent = bioPhoto.photoIdContent;
                bioPhotoBd.lastUpdated = bioPhoto.lastUpdated;
                bioPhotoBd.isSync = bioPhoto.isSync;
                bioPhotosDao.update(bioPhotoBd);
            }
        }
    }

    public int countAllBioPhotosForUser(int userId) {
        return bioPhotosDao.countAllBioPhotosForUser(userId);
    }

    public void deleteAll() {
        bioPhotosDao.deleteAll();
        bioPhotoFeaturesDao.deleteAll();
    }

    public void deleteForUser(int userId) {
        bioPhotosDao.deleteForUser(userId);
        bioPhotoFeaturesDao.deleteForUser(userId);
    }

}
