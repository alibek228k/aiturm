package kz.devs.aiturm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Apartment implements Parcelable {
    private String userID, apartmentID, description, id, buildingType, buildingAddress;
    private long timeStamp;

    private String preferences;
    private int numberOfRoommates;
    private double latitude, longitude;
    private List<String> imageUrl;
    private int price;


    protected Apartment(Parcel in) {
        userID = in.readString();
        apartmentID = in.readString();
        description = in.readString();
        id = in.readString();
        buildingType = in.readString();
        buildingAddress = in.readString();
        timeStamp = in.readLong();
        preferences = in.readString();
        numberOfRoommates = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        imageUrl = in.createStringArrayList();
        price = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userID);
        dest.writeString(apartmentID);
        dest.writeString(description);
        dest.writeString(id);
        dest.writeString(buildingType);
        dest.writeString(buildingAddress);
        dest.writeLong(timeStamp);
        dest.writeString(preferences);
        dest.writeInt(numberOfRoommates);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeStringList(imageUrl);
        dest.writeInt(price);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Apartment> CREATOR = new Creator<Apartment>() {
        @Override
        public Apartment createFromParcel(Parcel in) {
            return new Apartment(in);
        }

        @Override
        public Apartment[] newArray(int size) {
            return new Apartment[size];
        }
    };


    public String getBuildingAddress() {
        return buildingAddress;
    }


    public String getBuildingType() {
        return buildingType;
    }

    public String getPreferences() {
        return preferences;
    }


    public long getTimeStamp() {
        return timeStamp;
    }

    public String getUserID() {
        return userID;
    }

    public String getDescription() {
        return description;
    }

    public int getNumberOfRoommates() {
        return numberOfRoommates;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<String> getImageUrl() {
        return imageUrl;
    }

    public int getPrice() {
        return price;
    }

    public String getApartmentID() {
        return apartmentID;
    }

    public String getId() {
        return id;
    }

    public void setApartmentID(String apartmentID) {
        this.apartmentID = apartmentID;
    }

    Apartment() {
    }


}





