package neetsdkasu.fencapp;

import android.app.Application;

public class MyApplication extends Application {
    UserLocalDataSource localDataSource = new UserLocalDataSource(this);
    AppContainer appContainer = new AppContainer(localDataSource);
}