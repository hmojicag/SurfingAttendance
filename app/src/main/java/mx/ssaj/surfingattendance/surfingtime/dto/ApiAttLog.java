package mx.ssaj.surfingattendance.surfingtime.dto;

public class ApiAttLog {

    private int user;

    // yyyy-MM-dd HH:mm:ss
    private String verifyTime;

    // 1  - Fingerprint verification
    // 3  - Password verification
    // 4  - Card verification
    // 15 - Face verification
    private int verifyType;

    /* Verification photo if done by Face Recognition */
    /* ------------------------------------------------------------------------------------------*/
    private String photoIdName;

    /**The size of user photo data in Base64 format*/
    private int photoIdSize;

    private String photoIdContent;
    /* ------------------------------------------------------------------------------------------*/

    /** ---------------------------------------------------------------------------------------- **/
    /** Getters and Setters **/

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public String getVerifyTime() {
        return verifyTime;
    }

    public void setVerifyTime(String verifyTime) {
        this.verifyTime = verifyTime;
    }

    public int getVerifyType() {
        return verifyType;
    }

    public void setVerifyType(int verifyType) {
        this.verifyType = verifyType;
    }

    public String getPhotoIdName() {
        return photoIdName;
    }

    public void setPhotoIdName(String photoIdName) {
        this.photoIdName = photoIdName;
    }

    public int getPhotoIdSize() {
        return photoIdSize;
    }

    public void setPhotoIdSize(int photoIdSize) {
        this.photoIdSize = photoIdSize;
    }

    public String getPhotoIdContent() {
        return photoIdContent;
    }

    public void setPhotoIdContent(String photoIdContent) {
        this.photoIdContent = photoIdContent;
    }
}
