package neetsdkasu.fencapp;

import android.net.Uri;

import neetsdkasu.misc.ResultAdapter;
import neetsdkasu.misc.Value;

class FencViewModel {

    final Value<String> decError = new Value<>();
    final Value<String> origFileName = new Value<>("");
    final Value<Boolean> exportOrigFileResult = new Value<>();

    final Value<String> encError = new Value<>();
    final Value<String> fencFileName = new Value<>("");
    final Value<Boolean> exportFencFileResult = new Value<>();

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

    void exportOrigFile(Uri uri) {
        this.fencRepository.exportOrigFile(uri, new ResultAdapter<Void, Void>() {
            @Override
            public void onSuccess(Void nodata) {
                FencViewModel.this.exportOrigFileResult.setValue(true);
            }
            @Override
            public void onError(Exception ex) {
                FencViewModel.this.exportOrigFileResult.setValue(false);
            }
        });
    }

    void enc(String password, Uri uri) {
        this.fencRepository.enc(password, uri, new ResultAdapter<String, String>() {
            @Override
            public void onSuccess(String filename) {
                FencViewModel.this.fencFileName.setValue(filename);
            }
            @Override
            public void onFailure(String reason) {
                FencViewModel.this.encError.setValue(reason);
            }
            @Override
            public void onError(Exception ex) {
                FencViewModel.this.encError.setValue(ex.getMessage());
            }
        });
    }

    void exportFencFile(Uri uri) {
        this.fencRepository.exportFencFile(uri, new ResultAdapter<Void, Void>() {
            @Override
            public void onSuccess(Void nodata) {
                FencViewModel.this.exportFencFileResult.setValue(true);
            }
            @Override
            public void onError(Exception ex) {
                FencViewModel.this.exportFencFileResult.setValue(false);
            }
        });
    }
    
}