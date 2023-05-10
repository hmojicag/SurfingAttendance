package mx.ssaj.surfingattendanceapp.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import mx.ssaj.surfingattendanceapp.data.model.BioPhotoFeatures;

@Dao
public interface BioPhotoFeaturesDao {

    @Query("SELECT * FROM BioPhotoFeatures")
    List<BioPhotoFeatures> getAll();

    @Query("SELECT * FROM BioPhotoFeatures WHERE user = :userId AND type = :type")
    BioPhotoFeatures findById(int userId, int type);

    @Update(entity = BioPhotoFeatures.class)
    void update(BioPhotoFeatures bioPhotoFeatures);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(BioPhotoFeatures bioPhotoFeatures);

    @Query("DELETE FROM BioPhotoFeatures")
    void deleteAll();

    @Query("DELETE FROM BioPhotoFeatures WHERE user = :userId")
    void deleteForUser(int userId);

}
