package guru.ioio.golf;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import guru.ioio.golf.databinding.ActivityModelBinding;

public class ModelActivity extends AppCompatActivity {
    private ActivityModelBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_model);
    }
}
