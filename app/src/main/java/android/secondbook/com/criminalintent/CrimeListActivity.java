package android.secondbook.com.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Created by Administrator on 2017/8/15.
 */

public class CrimeListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }
}
