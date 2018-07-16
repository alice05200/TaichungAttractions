package aeight.sami.taichungattractions;

/**
 * Created by User on 2018/7/13.
 */
public class Location {
    //景點ID、狀態、名稱、簡述、介紹(html)、鄉鎮市區、地址、東經、北緯、電話、大眾運輸、門票資訊、行車資訊、停車資訊、旅遊叮嚀
    private String status, name, intro, address, telNum, lagitude, longitude;

    public Location(String status, String name, String intro, String address, String telNum){
        this.status = status;
        this.name = name;
        this.intro = intro;
        this.address = address;
        this.telNum = telNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelNum() {
        return telNum;
    }

    public void setTelNum(String telNum) {
        this.telNum = telNum;
    }
}

