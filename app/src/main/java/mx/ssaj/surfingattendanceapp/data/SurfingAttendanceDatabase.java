package mx.ssaj.surfingattendanceapp.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import mx.ssaj.surfingattendanceapp.data.dao.AttendanceRecordDao;
import mx.ssaj.surfingattendanceapp.data.dao.BioPhotoFeaturesDao;
import mx.ssaj.surfingattendanceapp.data.dao.BioPhotosDao;
import mx.ssaj.surfingattendanceapp.data.dao.SurfingTimeCommandsDao;
import mx.ssaj.surfingattendanceapp.data.dao.UsersDao;
import mx.ssaj.surfingattendanceapp.data.model.Areas;
import mx.ssaj.surfingattendanceapp.data.model.AttendanceRecord;
import mx.ssaj.surfingattendanceapp.data.model.BioPhotoFeatures;
import mx.ssaj.surfingattendanceapp.data.model.BioPhotos;
import mx.ssaj.surfingattendanceapp.data.model.SurfingTimeCommand;
import mx.ssaj.surfingattendanceapp.data.model.Users;
import mx.ssaj.surfingattendanceapp.data.model.UsersAreas;

@Database(entities = {
        Areas.class,
        BioPhotos.class,
        BioPhotoFeatures.class,
        AttendanceRecord.class,
        SurfingTimeCommand.class,
        Users.class,
        UsersAreas.class
}, version = 1, exportSchema = false)
public abstract class SurfingAttendanceDatabase extends RoomDatabase {
    public abstract BioPhotosDao bioPhotosDao();
    public abstract UsersDao usersDao();
    public abstract BioPhotoFeaturesDao bioPhotoFeaturesDao();
    public abstract AttendanceRecordDao attendanceRecordDao();
    public abstract SurfingTimeCommandsDao surfingTimeCommandsDao();

    private static volatile SurfingAttendanceDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static SurfingAttendanceDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SurfingAttendanceDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    SurfingAttendanceDatabase.class, "surfing_attendance_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Populate the database in the background.
                // If you want to start with more records, just add them.
//                UsersDao dao = INSTANCE.usersDao();
//                AttendanceRecordDao attendanceRecordDao = INSTANCE.attendanceRecordDao();
//                attendanceRecordDao.deleteAll();
//                dao.deleteAll();

//                int usersCount = dao.getUsersCount();

//                if (usersCount > 0) {
//                    // Return if database is already populated
//                    return;
//                }

//                Users user = new Users();
//                user.user = 1;
//                user.name = "Hazael Mojica";
//                dao.insert(user);
//
//                user = new Users();
//                user.user = 2;
//                user.name = "Juventino Hernandez";
//                dao.insert(user);
//
//                user = new Users();
//                user.user = 3;
//                user.name = "Alberto Galvan";
//                dao.insert(user);
            }
        };

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            databaseWriteExecutor.execute(runnable);
        }

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(runnable);
        }
    };

}
