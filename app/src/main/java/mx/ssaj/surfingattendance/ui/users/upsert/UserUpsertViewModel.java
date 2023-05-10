package mx.ssaj.surfingattendance.ui.users.upsert;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import mx.ssaj.surfingattendance.data.model.Users;
import mx.ssaj.surfingattendance.data.repositories.UsersRepository;

public class UserUpsertViewModel extends AndroidViewModel {

    private UsersRepository usersRepository;

    public UserUpsertViewModel(Application application) {
        super(application);
        usersRepository = new UsersRepository(application);
    }

    public void insert(Users user) {
        usersRepository.insert(user);
    }

    public void update(Users user) {
        usersRepository.update(user);
    }

    public Users findById(int userId) {
        return usersRepository.findFullById(userId);
    }

    public int nextId() {
        return usersRepository.nextId();
    }

}