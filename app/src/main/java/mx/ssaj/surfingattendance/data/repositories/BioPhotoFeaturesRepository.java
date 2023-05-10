package mx.ssaj.surfingattendance.data.repositories;

import android.app.Application;

import mx.ssaj.surfingattendance.data.SurfingAttendanceDatabase;
import mx.ssaj.surfingattendance.data.dao.BioPhotoFeaturesDao;
import mx.ssaj.surfingattendance.data.model.BioPhotoFeatures;

public class BioPhotoFeaturesRepository {

    private BioPhotoFeaturesDao bioPhotoFeaturesDao;

    public BioPhotoFeaturesRepository(Application application) {
        SurfingAttendanceDatabase surfingAttendanceDatabase = SurfingAttendanceDatabase.getDatabase(application);
        bioPhotoFeaturesDao = surfingAttendanceDatabase.bioPhotoFeaturesDao();
    }

    public BioPhotoFeatures findById(int userId, int type) {
        return bioPhotoFeaturesDao.findById(userId, type);
    }

    public void update(BioPhotoFeatures bioPhotoFeatures) {
        bioPhotoFeaturesDao.update(bioPhotoFeatures);
    }

    public void insert(BioPhotoFeatures bioPhotoFeatures) {
        bioPhotoFeaturesDao.insert(bioPhotoFeatures);
    }

    public void upsert(BioPhotoFeatures bioPhotoFeatures) {
        BioPhotoFeatures bioPhotoFeaturesBd = findById(bioPhotoFeatures.user, bioPhotoFeatures.type);
        if (bioPhotoFeaturesBd == null) {
            bioPhotoFeaturesDao.insert(bioPhotoFeatures);
        } else {
            // Update editable fields
            bioPhotoFeaturesBd.features = bioPhotoFeatures.features;
            bioPhotoFeaturesDao.update(bioPhotoFeaturesBd);
        }
    }

}
