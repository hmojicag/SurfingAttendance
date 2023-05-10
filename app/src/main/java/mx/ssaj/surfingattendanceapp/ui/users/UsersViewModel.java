package mx.ssaj.surfingattendanceapp.ui.users;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import mx.ssaj.surfingattendanceapp.data.model.Users;
import mx.ssaj.surfingattendanceapp.data.repositories.UsersRepository;

public class UsersViewModel extends AndroidViewModel {

    private UsersRepository usersRepository;

    public UsersViewModel(Application application) {
        super(application);
        usersRepository = new UsersRepository(application);
    }

    public LiveData<List<Users>> getAllUsersLive() {
        return usersRepository.getAllUsersLive();
    }
}