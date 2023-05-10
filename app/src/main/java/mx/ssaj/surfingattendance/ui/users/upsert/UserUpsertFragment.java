package mx.ssaj.surfingattendance.ui.users.upsert;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import org.apache.commons.lang3.StringUtils;
import mx.ssaj.surfingattendance.R;
import mx.ssaj.surfingattendance.data.SurfingAttendanceDatabase;
import mx.ssaj.surfingattendance.data.model.BioPhotos;
import mx.ssaj.surfingattendance.data.model.Users;
import mx.ssaj.surfingattendance.databinding.FragmentUsersUpsertBinding;
import mx.ssaj.surfingattendance.detection.env.Logger;
import mx.ssaj.surfingattendance.util.Literals;
import mx.ssaj.surfingattendance.util.Util;

public class UserUpsertFragment extends Fragment {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "UserUpsertFragment";
    private boolean shouldExecuteOnResume;
    private FragmentUsersUpsertBinding binding;
    private int userId;
    private boolean isNew;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        UserUpsertViewModel userUpsertViewModel = new ViewModelProvider(this).get(UserUpsertViewModel.class);
        shouldExecuteOnResume = false;
        binding = FragmentUsersUpsertBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // User Id to edit, if -1 then it's newly addition
        int userId = UserUpsertFragmentArgs.fromBundle(getArguments()).getUserId();
        isNew = userId == -1;
        this.userId = userId;

        if (isNew) {// New user getting added
            // Hide Delete button and SurfingTime sync message
            binding.buttonDelete.setVisibility(View.INVISIBLE);
            binding.textViewUserUpsertSurfingtimesync.setVisibility(View.INVISIBLE);

            SurfingAttendanceDatabase.databaseWriteExecutor.execute(() -> {
                int nextUserId = userUpsertViewModel.nextId();
                binding.editTextUpsertUserId.setText(String.valueOf(nextUserId));

            });
        } else {// Edit existing
            SurfingAttendanceDatabase.databaseWriteExecutor.execute(() -> {
                Users user = userUpsertViewModel.findById(userId);
                requireActivity().runOnUiThread(() -> {
                    binding.editTextUpsertUserId.setText(String.valueOf(user.user));
                    binding.editTextUpsertName.setText(user.name);
                    binding.editTextUpsertCard.setText(user.mainCard);
                    binding.editTextUpsertPassword.setText(user.password);
                    binding.switchUpsertActive.setChecked(StringUtils.equals(user.status, "A"));
                    checkUserSyncedToSurfingTime(user);

                    // Set user profile photo and thumbnail BioPhoto
                    fetchAndSetUserPhotos(user);

                    // ON CLICK CHECK PROFILE PHOTO
                    // -----------------------------------------------------------------------------------------
                    binding.circleimageviewUpsertUserPicture.setOnClickListener(view -> {
                        if (!isNew) {
                            // Fetching BioPhoto FullPhoto as Profile Picture to shown
                            Bitmap profilePicture = user.findFirstBioPhotoPhoto();
                            if (profilePicture != null) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                                LayoutInflater inflaterAlertDialog = getLayoutInflater();
                                View dialogLayout = inflaterAlertDialog.inflate(R.layout.show_profile_pic, null);
                                ImageView showProfilePicImageView = dialogLayout.findViewById(R.id.imageView_showProfilePic);
                                showProfilePicImageView.setImageBitmap(profilePicture);
                                builder.setView(dialogLayout);
                                builder.show();
                            }
                        }
                    });
                });
            });

            // Disable the edition of the Id
            binding.editTextUpsertUserId.setEnabled(false);
        }

        // ON CLICK SAVE
        // -----------------------------------------------------------------------------------------
        binding.buttonSave.setOnClickListener(view -> {
            SurfingAttendanceDatabase.databaseWriteExecutor.execute(() -> {
                Users user;
                if (isNew) {
                    LOGGER.i(TAG, "Creating new user");
                    user = new Users();
                    user.user = Integer.parseInt(binding.editTextUpsertUserId.getText().toString());
                } else {
                    LOGGER.i(TAG, "Updating existing user");
                    user = userUpsertViewModel.findById(userId);
                }

                // Set editable fields
                user.name = binding.editTextUpsertName.getText().toString();
                user.mainCard = binding.editTextUpsertCard.getText().toString();
                user.password = binding.editTextUpsertPassword.getText().toString();
                user.status = binding.switchUpsertActive.isChecked() ? "A" : "B";
                user.lastUpdated = Util.getDateTimeNow();
                user.isSync = Literals.FALSE;

                // Update or insert a record
                if (isNew) {
                    userUpsertViewModel.insert(user);
                } else {
                    userUpsertViewModel.update(user);
                }
            });

            // Return one screen back (To Users screen)
            Navigation.findNavController(view).popBackStack();
        });

        // ON CLICK CANCEL
        binding.buttonCancel.setOnClickListener(view -> {
            // Return one screen back (To Users screen)
            Navigation.findNavController(view).popBackStack();
        });

        // ON CLICK SET BIOPHOTO
        // -----------------------------------------------------------------------------------------
        binding.imageviewUpsertBiophoto.setOnClickListener(view -> {
            if (isNew) {
                // Can't set a BioPhoto to a new User
                Toast.makeText(getActivity().getApplicationContext(), R.string.user_upsert_set_bioPhoto_not_saved,Toast.LENGTH_SHORT).show();
            } else {
                // Navigate to Activity using safe args
                mx.ssaj.surfingattendance.ui.users.upsert.UserUpsertFragmentDirections.ActionUsersUpsertFragmentToUpdateBioPhotoFaceDetectionActivity action =
                        UserUpsertFragmentDirections.actionUsersUpsertFragmentToUpdateBioPhotoFaceDetectionActivity();
                action.setUserId(userId);
                Navigation.findNavController(view).navigate(action);
            }
        });

        // ON CLICK SET DELETE
        // -----------------------------------------------------------------------------------------
        if (!isNew) {
            binding.buttonDelete.setOnClickListener(view -> {
                SurfingAttendanceDatabase.databaseWriteExecutor.execute(() -> {
                    userUpsertViewModel.delete(userId);
                });

                // Return one screen back (To Users screen)
                Navigation.findNavController(view).popBackStack();
            });
        }

        return root;
    }

    private void checkUserSyncedToSurfingTime(Users user) {
        boolean isUserFullySyncedToSurfingTime = false;

        isUserFullySyncedToSurfingTime = user.isSync == Literals.TRUE;
        BioPhotos bioPhoto = user.findFirstBioPhoto();
        if (bioPhoto != null) {
            isUserFullySyncedToSurfingTime = isUserFullySyncedToSurfingTime && bioPhoto.isSync == Literals.TRUE;
        }

        if (isUserFullySyncedToSurfingTime) {
            binding.textViewUserUpsertSurfingtimesync.setText(R.string.textView_SurfingTimeSync_Yes);
            binding.textViewUserUpsertSurfingtimesync.setTextColor(getContext().getColor(R.color.ok));
        } else {
            binding.textViewUserUpsertSurfingtimesync.setText(R.string.textView_SurfingTimeSync_No);
            binding.textViewUserUpsertSurfingtimesync.setTextColor(getContext().getColor(R.color.danger));
        }
    }

    public void fetchAndSetUserPhotos() {
        // Fetch Photos again from database
        SurfingAttendanceDatabase.databaseWriteExecutor.execute(() -> {
            UserUpsertViewModel userUpsertViewModel = new ViewModelProvider(this).get(UserUpsertViewModel.class);
            Users user = userUpsertViewModel.findById(userId);
            if (user != null) {
                requireActivity().runOnUiThread(() -> {
                    // Refresh photos
                    fetchAndSetUserPhotos(user);
                });
            }
        });
    }

    public void fetchAndSetUserPhotos(Users user) {
        Bitmap profilePicture = user.getPhoto();
        Bitmap thumbNailPhoto = user.findFirstBioPhotoThumbnailPhoto();
        if (profilePicture != null) {
            binding.circleimageviewUpsertUserPicture.setImageBitmap(profilePicture);
        }
        if (thumbNailPhoto != null) {
            binding.imageviewUpsertBiophoto.setImageBitmap(thumbNailPhoto);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // OnResume is executed when the fragment gets first created and when resuming the fragment
        if(shouldExecuteOnResume) {
            // Execute this code ONLY when coming back from another activity, not when the fragment
            // first gets created
            fetchAndSetUserPhotos();
        } else {
            shouldExecuteOnResume = true;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}