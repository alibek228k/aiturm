package kz.devs.aiturm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PersonalPostModel implements Parcelable {

    private String date, description, userID, geoHash, locality, subLocality;
    private ArrayList<String> buildingTypes;
    private int price;
    private String preferences, postID;
    private long timeStamp;

    public PersonalPostModel() {
    }

    protected PersonalPostModel(Parcel in) {
        date = in.readString();
        description = in.readString();
        userID = in.readString();
        postID = in.readString();
        geoHash = in.readString();
        locality = in.readString();
        subLocality = in.readString();
        buildingTypes = in.createStringArrayList();
        price = in.readInt();
        preferences = in.readString();
        postID = in.readString();
        timeStamp = in.readLong();
    }

    public static final Creator<PersonalPostModel> CREATOR = new Creator<PersonalPostModel>() {
        @Override
        public PersonalPostModel createFromParcel(Parcel in) {
            return new PersonalPostModel(in);
        }

        @Override
        public PersonalPostModel[] newArray(int size) {
            return new PersonalPostModel[size];
        }
    };

    public ArrayList<String> getBuildingTypes() {
        return buildingTypes;
    }

    public void setBuildingTypes(ArrayList<String> buildingTypes) {
        this.buildingTypes = buildingTypes;
    }


    public PersonalPostModel(String date, String description, String userID, String id, int price, double latitude, double longitude, String preferences) {
        this.date = date;
        this.description = description;
        this.userID = userID;
        this.postID = id;
        this.price = price;
        this.preferences = preferences;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getLocality() {
        return locality;
    }

    public String getSubLocality() {
        return subLocality;
    }

    public static Creator<PersonalPostModel> getCREATOR() {
        return CREATOR;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getId() {
        return postID;
    }

    public void setId(String id) {
        this.postID = id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeString(description);
        dest.writeString(userID);
        dest.writeString(postID);
        dest.writeString(geoHash);
        dest.writeString(locality);
        dest.writeString(subLocality);
        dest.writeStringList(buildingTypes);
        dest.writeInt(price);
        dest.writeString(preferences);
        dest.writeString(postID);
        dest.writeLong(timeStamp);
    }
}
