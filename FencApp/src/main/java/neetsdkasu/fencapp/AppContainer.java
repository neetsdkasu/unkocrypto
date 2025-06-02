package neetsdkasu.fencapp;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class AppContainer {
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    Handler mainThreadHandler = Handler.createAsync(Looper.getMainLooper());

    UserLocalDataSource localDataSource;
    FencRepository fencRepository;

    AppContainer(UserLocalDataSource localDataSource) {
        this.localDataSource = localDataSource;
        this.fencRepository = new FencRepository(localDataSource, executorService, mainThreadHandler);
    }
}
