package neetsdkasu.misc;

public interface ResultCallback<V,E> {

    void onSuccess(V data);
    void onFailure(E data);
    void onError(Exception ex);

}