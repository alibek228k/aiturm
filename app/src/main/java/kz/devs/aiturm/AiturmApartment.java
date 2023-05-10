package kz.devs.aiturm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class AiturmApartment implements Parcelable {
    String apartmentID, adminID;
    HashMap<String,String> apartmentMembers;
    Map<String, ExpensesCard> expensesCard;
    Map<String, TasksCard> taskCard;

    public Map<String, ApartmentLogs> getLogs() {
        return logs;
    }

    public void setLogs(Map<String, ApartmentLogs> logs) {
        this.logs = logs;
    }

    Map<String , ApartmentLogs> logs;

    public Map<String ,ExpensesCard> getExpensesCard() {
        return expensesCard;
    }

    public void setExpensesCard(Map<String,ExpensesCard> expensesCard) {
        this.expensesCard = expensesCard;
    }

    public Map<String,TasksCard> getTaskCard() {
        return taskCard;
    }

    public void setTaskCard(Map<String,TasksCard> taskCard) {
        this.taskCard = taskCard;
    }

    public AiturmApartment() {

    }

    public AiturmApartment(String apartmentID, String adminID, HashMap<String, String> apartmentMembers, Map<String ,TasksCard> taskCard, Map<String,ExpensesCard> expensesCard) {
        this.apartmentID = apartmentID;
        this.adminID = adminID;
        this.apartmentMembers = apartmentMembers;
        this.expensesCard = expensesCard;
        this.taskCard = taskCard;
    }


    public String getApartmentID() {
        return apartmentID;
    }

    public void setApartmentID(String apartmentID) {
        this.apartmentID = apartmentID;
    }

    public String getAdminID() {
        return adminID;
    }

    public void setAdminID(String adminID) {
        this.adminID = adminID;
    }

    public HashMap<String,String> getApartmentMembers() {
        return apartmentMembers;
    }

    public void setApartmentMembers(HashMap<String,String> apartmentMembers) {
        this.apartmentMembers = apartmentMembers;
    }

    protected AiturmApartment(Parcel in) {
        apartmentID = in.readString();
        adminID = in.readString();
        apartmentMembers=in.readHashMap(HashMap.class.getClassLoader());



    }

    public static final Creator<AiturmApartment> CREATOR = new Creator<AiturmApartment>() {
        @Override
        public AiturmApartment createFromParcel(Parcel in) {
            return new AiturmApartment(in);
        }

        @Override
        public AiturmApartment[] newArray(int size) {
            return new AiturmApartment[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(apartmentID);
        parcel.writeString(adminID);
        parcel.writeMap(apartmentMembers);

    }
}
