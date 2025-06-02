package neetsdkasu.misc;

public class ResultAdapter<V,E> implements ResultCallback<V,E> {

    public ResultAdapter() {}

    public void onSuccess(V data) {}

    public void onFailure(E data) {}

    public void onError(Exception ex) {}
}