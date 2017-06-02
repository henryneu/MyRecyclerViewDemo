package neu.cn.myrecyclerview;

/**
 * Created by neuHenry on 2017/6/1.
 */

public class Fruit {

    private int imageID;
    private String imageName;

    public Fruit(int imageID, String imageName) {
        this.imageID = imageID;
        this.imageName = imageName;
    }

    public int getImageID() {
        return imageID;
    }

    public String getImageName() {
        return imageName;
    }
}
