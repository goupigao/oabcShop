package cc.oabc.shop;

import android.app.Fragment;
import android.view.View;

import rx.Subscription;

/**
 * Created by Administrator on 2016/7/30.
 */
public abstract class BaseFragment extends Fragment {
    protected Subscription subscription;
    public View rootView;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unsubscribe();
    }

    protected void unsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
