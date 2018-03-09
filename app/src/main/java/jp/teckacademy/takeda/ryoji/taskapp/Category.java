package jp.teckacademy.takeda.ryoji.taskapp;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ryojitakeda on 2018/03/08.
 */

public class Category extends RealmObject implements Serializable {

    @PrimaryKey
    private int id;
    private String category;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return category;
    }
}
