package mx.ssaj.surfingattendance.ui.attrecords;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import mx.ssaj.surfingattendance.R;
import mx.ssaj.surfingattendance.data.SurfingAttendanceDatabase;
import mx.ssaj.surfingattendance.data.dto.SearchAttendanceRecordsQuery;
import mx.ssaj.surfingattendance.data.model.AttendanceRecord;
import mx.ssaj.surfingattendance.databinding.FragmentAttrecordsBinding;

public class AttRecordsFragment extends Fragment {

    // https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data#guava-livedata
    // https://developer.android.com/codelabs/android-paging#15

    private FragmentAttrecordsBinding binding;
    // Request code for creating a PDF document.
    private static final int CREATE_FILE = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AttRecordsViewModel attRecordsViewModel = new ViewModelProvider(this).get(AttRecordsViewModel.class);
        binding = FragmentAttrecordsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Export CSV Button Click
        binding.buttonExport.setOnClickListener(view -> {
            promptUserToCreateCsvFile();
        });

        RecyclerView recyclerViewAttRecords = binding.recyclerViewAttrecords;
        final AttRecordsPagingDataAdapter attRecordsPagingDataAdapter = new AttRecordsPagingDataAdapter(new AttRecordsPagingDataAdapter.AttendaceRecordDiff());
        recyclerViewAttRecords.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAttRecords.setAdapter(attRecordsPagingDataAdapter);

        SearchAttendanceRecordsQuery query = new SearchAttendanceRecordsQuery();// TODO Build the query here if needed
        attRecordsViewModel.searchAttendanceRecords(query).observe(this.getViewLifecycleOwner(), pagingData -> {
            attRecordsPagingDataAdapter.submitData(getViewLifecycleOwner().getLifecycle(), pagingData);
        });

        return root;
    }

    private void promptUserToCreateCsvFile() {
        File downloadFolder = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        Uri pickerInitialUri = Uri.parse(downloadFolder.toString());
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "attlogs.csv");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        startActivityForResult(intent, CREATE_FILE);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * The execution flow comes back here as a result from the Intent created on method
     * #promptUserToCreateCsvFile()
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that the user selected.
            if (resultData != null) {
                Uri csvFileUri = resultData.getData();
                saveAttendanceLogsToCsvFile(csvFileUri);
            }
        }
    }

    private void saveAttendanceLogsToCsvFile(Uri csvFileUri) {
        SurfingAttendanceDatabase.databaseWriteExecutor.execute(() -> {
            AttRecordsViewModel attRecordsViewModel = new ViewModelProvider(this).get(AttRecordsViewModel.class);
            List<AttendanceRecord> attendanceRecords = attRecordsViewModel.getAllAttendanceRecords();
            String header = getString(R.string.csvHeaders);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(header).append("\n");
            for (AttendanceRecord attendanceRecord: attendanceRecords) {
                stringBuilder.append(attendanceRecord.verifyTime).append(",");
                stringBuilder.append(attendanceRecord.user).append(",");
                stringBuilder.append(attendanceRecord.userObj.name).append(",");
                stringBuilder.append(attendanceRecord.verifyTypeStr).append(",");
                stringBuilder.append(attendanceRecord.isSync).append("\n");
            }

            writeToFile(csvFileUri, stringBuilder.toString());
        });
    }

    private void writeToFile(Uri uri, String fileContent) {
        try {
            ParcelFileDescriptor filed = getActivity().getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream = new FileOutputStream(filed.getFileDescriptor());
            fileOutputStream.write(fileContent.getBytes());
            fileOutputStream.close();
            filed.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}