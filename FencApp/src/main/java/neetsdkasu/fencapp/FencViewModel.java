package neetsdkasu.fencapp;

import android.net.Uri;

import neetsdkasu.misc.ResultAdapter;
import neetsdkasu.misc.Value;

class FencViewModel {

    final Value<String> decError = new Value<>();
    final Value<String> origFileName = new Value<>("");
    final Value<Boolean> exportResult = new Value<>();

    private final FencRepository fencRepository;

    FencViewModel(FencRepository fencRepository) {
        this.fencRepository = fencRepository;
    }

    void dec(String password, Uri uri) {
        this.fencRepository.dec(password, uri, new ResultAdapter<String, String>() {
            @Override
            public void onSuccess(String filename) {
                FencViewModel.this.origFileName.setValue(filename);
            }
            @Override
            public void onFailure(String reason) {
                FencViewModel.this.decError.setValue(reason);
            }
            @Override
            public void onError(Exception ex) {
                FencViewModel.this.decError.setValue(ex.getMessage());
            }
        });
    }

    void export(Uri uri) {
        this.fencRepository.export(uri, new ResultAdapter<Void, Void>() {
            @Override
            public void onSuccess(Void nodata) {
                FencViewModel.this.exportResult.setValue(true);
            }
            @Override
            public void onError(Exception ex) {
                FencViewModel.this.exportResult.setValue(false);
            }
        });
    }
    
}