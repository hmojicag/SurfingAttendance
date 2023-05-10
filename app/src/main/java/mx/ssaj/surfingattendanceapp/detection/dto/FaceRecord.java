package mx.ssaj.surfingattendanceapp.detection.dto;

import android.graphics.Bitmap;

import com.google.mlkit.vision.face.Face;

import mx.ssaj.surfingattendanceapp.detection.tflite.SimilarityClassifier;

public class FaceRecord {

    private int userId;

    private String name;

    private Face face;

    private Bitmap faceFullPhoto;

    private Bitmap faceThumbnail;

    private Float confidence;

    private SimilarityClassifier.Recognition recognition;

    public FaceRecord() {
    }

    public FaceRecord(Face face, Bitmap faceImage) {
        this.face = face;
        this.faceFullPhoto = faceImage;
    }

    public FaceRecord(String name, Face face, Bitmap faceImage, Bitmap faceCropped, Float confidence, SimilarityClassifier.Recognition recognition) {
        this.name = name;
        this.face = face;
        this.faceFullPhoto = faceImage;
        this.faceThumbnail = faceCropped;
        this.confidence = confidence;
        this.recognition = recognition;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Face getFace() {
        return face;
    }

    public void setFace(Face face) {
        this.face = face;
    }

    public Bitmap getFaceFullPhoto() {
        return faceFullPhoto;
    }

    public void setFaceFullPhoto(Bitmap faceFullPhoto) {
        this.faceFullPhoto = faceFullPhoto;
    }

    public Bitmap getFaceThumbnail() {
        return faceThumbnail;
    }

    public void setFaceThumbnail(Bitmap faceThumbnail) {
        this.faceThumbnail = faceThumbnail;
    }

    public Float getConfidence() {
        return confidence;
    }

    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }

    public SimilarityClassifier.Recognition getRecognition() {
        return recognition;
    }

    public void setRecognition(SimilarityClassifier.Recognition recognition) {
        this.recognition = recognition;
    }

    @Override
    public String toString() {
        return "FaceRecognized{" +
                "name='" + name + '\'' +
                ", face=" + face +
                ", faceFullPhoto=" + faceFullPhoto +
                ", faceThumbnail=" + faceThumbnail +
                ", confidence=" + confidence +
                '}';
    }
}
