package mx.ssaj.surfingattendanceapp.detectionwrappers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.util.List;

import mx.ssaj.surfingattendanceapp.data.model.BioPhotoFeatures;
import mx.ssaj.surfingattendanceapp.data.model.BioPhotos;
import mx.ssaj.surfingattendanceapp.detection.dto.FaceRecord;
import mx.ssaj.surfingattendanceapp.detection.env.Logger;
import mx.ssaj.surfingattendanceapp.detection.tflite.SimilarityClassifier;
import mx.ssaj.surfingattendanceapp.detection.tflite.TFLiteObjectDetectionAPIModel;
import mx.ssaj.surfingattendanceapp.util.BioDataType;
import mx.ssaj.surfingattendanceapp.util.Literals;
import mx.ssaj.surfingattendanceapp.util.Util;

/**
 * A wrapper classes over the detection package.
 * It's aimed to provide a single class for extracting face features and
 * doing face recognition in a synchronous way
 */
public class FaceDetectionSynchronousService {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "FaceDetectionSynchronousService";
    private static final int TF_OD_API_INPUT_SIZE = 112;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_API_MODEL_FILE = "mobile_face_net.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";

    private FaceDetector faceDetector;
    private SimilarityClassifier detector;

    public FaceDetectionSynchronousService(Context context) {
        // Real-time contour detection of multiple faces
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();

        faceDetector = FaceDetection.getClient(options);

        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            context.getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
        } catch (final IOException e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(context, "Classifier could not be initialized for FaceDetectionSynchronousService", Toast.LENGTH_SHORT);
            toast.show();
            throw new RuntimeException(e);
        }
    }

    /**
     * Process passed in BioPhoto, extracts features and thumbnail from it and then sets them
     * in the corresponding fields of the passed in object
     * @param bioPhoto The BioPhoto object to be updated with new Features and Thumbnail
     * @throws Exception
     */
    public void setBioPhotoFeaturesToBioPhoto(BioPhotos bioPhoto) throws Exception {
        FaceRecord faceRecord = extractFaceFeaturesFromBioPhoto(bioPhoto);

        BioPhotoFeatures bioPhotoFeatures = new BioPhotoFeatures();
        bioPhotoFeatures.user = bioPhoto.user;
        bioPhotoFeatures.type = bioPhoto.type;
        bioPhotoFeatures.setFeatures((float[][]) faceRecord.getRecognition().getExtra());

        BioPhotos thumbNailBioPhoto = new BioPhotos();
        thumbNailBioPhoto.user = bioPhoto.user;
        thumbNailBioPhoto.type = BioDataType.BIOPHOTO_THUMBNAIL_JPG.getType();
        thumbNailBioPhoto.setBioPhotoContent(faceRecord.getFaceThumbnail());
        thumbNailBioPhoto.lastUpdated = Util.getDateTimeNow();
        thumbNailBioPhoto.isSync = Literals.FALSE;

        // Set into the passed object
        bioPhoto.Features = bioPhotoFeatures;
        bioPhoto.Thumbnail = thumbNailBioPhoto;
    }

    public FaceRecord extractFaceFeaturesFromBioPhoto(BioPhotos bioPhoto) throws Exception {
        try {
            Bitmap fullPhoto = bioPhoto.getPhoto();
            InputImage image = InputImage.fromBitmap(fullPhoto, 0);
            Task<List<Face>> detectFacesTask = faceDetector.process(image);
            List<Face> faces = Tasks.await(detectFacesTask);
            if (faces.size() > 1) {
                throw new Exception("Biophoto for user " + bioPhoto.user + " contains more than 1 Face");
            }
            Face face = faces.get(0);
            FaceRecord faceRecord = extractFeaturesFromFace(bioPhoto.getBioPhotoStringIdentifier(), fullPhoto, face);
            return faceRecord;
        } catch (Exception ex) {
            String errMsg = "Error while extracting face features from BioPhoto";
            LOGGER.e(TAG, ex, errMsg);
            throw new Exception(errMsg, ex);
        }
    }

    private FaceRecord extractFeaturesFromFace(String bioPhotoIdentifier, Bitmap fullPhoto, Face face) {
        Bitmap faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Bitmap.Config.ARGB_8888);
        Canvas cvFace = new Canvas(faceBmp);
        RectF boundingBox = new RectF(face.getBoundingBox());
        FaceRecord faceRecord = new FaceRecord();

        // translates portrait to origin and scales to fit input inference size
        float sx = ((float) TF_OD_API_INPUT_SIZE) / boundingBox.width();
        float sy = ((float) TF_OD_API_INPUT_SIZE) / boundingBox.height();
        Matrix matrix = new Matrix();
        matrix.postTranslate(-boundingBox.left, -boundingBox.top);
        matrix.postScale(sx, sy);

        // Substract faceBmp from fullPhoto according to boundingBox matrix
        cvFace.drawBitmap(fullPhoto, matrix, null);

        final List<SimilarityClassifier.Recognition> resultsAux = detector.recognizeImage(faceBmp, true);
        if (resultsAux.size() > 0) {
            SimilarityClassifier.Recognition result = resultsAux.get(0);
            final SimilarityClassifier.Recognition recognition = new SimilarityClassifier.Recognition(
                    "0", bioPhotoIdentifier, 0f, boundingBox);

            recognition.setLocation(boundingBox);
            recognition.setExtra(result.getExtra());
            faceRecord.setRecognition(recognition);
        } else {
            throw new RuntimeException("Biophoto for user doesn't have a recognized face");
        }

        faceRecord.setFace(face);
        faceRecord.setName(bioPhotoIdentifier);
        faceRecord.setFaceFullPhoto(fullPhoto);
        faceRecord.setFaceThumbnail(faceBmp);

        // Log extras for debugging purposes
        LOGGER.d(TAG, "Logging extras while recovering user " + bioPhotoIdentifier + " from DB: " + ArrayUtils.toString(faceRecord.getRecognition().getExtra()));

        return faceRecord;
    }

}
