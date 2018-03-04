package jp.teckacademy.takeda.ryoji.taskapp;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by ryojitakeda on 2018/03/04.
 */

public class TaskApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
