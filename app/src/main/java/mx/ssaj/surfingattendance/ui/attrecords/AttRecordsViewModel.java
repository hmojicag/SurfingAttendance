package mx.ssaj.surfingattendance.ui.attrecords;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import java.util.List;

import kotlinx.coroutines.CoroutineScope;
import mx.ssaj.surfingattendance.data.dto.SearchAttendanceRecordsQuery;
import mx.ssaj.surfingattendance.data.model.AttendanceRecord;
import mx.ssaj.surfingattendance.data.repositories.AttendanceRecordsRepository;

public class AttRecordsViewModel extends AndroidViewModel {

    private AttendanceRecordsRepository attendanceRecordsRepository;

    public AttRecordsViewModel(Application application) {
        super(application);
        attendanceRecordsRepository = new AttendanceRecordsRepository(application);
    }

    public List<AttendanceRecord> getAllAttendanceRecords() {
        return attendanceRecordsRepository.getAll();
    }

    public LiveData<List<AttendanceRecord>> getAllAttendanceRecordsLive() {
        return attendanceRecordsRepository.getAllAttendanceRecordsLive();
    }

    public LiveData<PagingData<AttendanceRecord>> searchAttendanceRecords(SearchAttendanceRecordsQuery query) {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
        Pager<Integer, AttendanceRecord> pager = new Pager<>(
                new PagingConfig(/* pageSize = */ AttRecordsPagingSource.PageSize),
                () -> new AttRecordsPagingSource(attendanceRecordsRepository, query));
        return PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), viewModelScope);
    }

}