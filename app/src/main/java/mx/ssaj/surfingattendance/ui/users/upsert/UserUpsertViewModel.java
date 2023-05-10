package mx.ssaj.surfingattendance.ui.users.upsert;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import mx.ssaj.surfingattendance.data.model.Users;
import mx.ssaj.surfingattendance.data.repositories.BioPhotosRepository;
import mx.ssaj.surfingattendance.data.repositories.UsersRepository;

public class UserUpsertViewModel extends AndroidViewModel {

    private UsersRepository usersRepository;
    private BioPhotosRepository bioPhotosRepository;

    public UserUpsertViewModel(Application application) {
        super(application);
        usersRepository = new UsersRepository(application);
        bioPhotosRepository = new BioPhotosRepository(application);
    }

    public void insert(Users user) {
        usersRepository.insert(user);
    }

    public void update(Users user) {
        usersRepository.update(user);
    }

    public void delete(int userId) {
        bioPhotosRepository.deleteForUser(userId);
        usersRepository.deleteById(userId);
    }

    public Users findById(int userId) {
        return usersRepository.findFullById(userId);
    }

    public int nextId() {
        return usersRepository.nextId();
    }

}