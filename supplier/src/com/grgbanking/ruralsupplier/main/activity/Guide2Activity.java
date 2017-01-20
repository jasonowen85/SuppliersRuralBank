package com.grgbanking.ruralsupplier.main.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.grgbanking.ruralsupplier.R;
import com.grgbanking.ruralsupplier.main.fragment.guideFragment;

/**
 * @author dwtedx
 *         功能描述：主程序入口类
 */
public class Guide2Activity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_guide);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.layout_guide, guideFragment.newInstance(guideFragment.NODIR));
        ft.commit();
    }
}
