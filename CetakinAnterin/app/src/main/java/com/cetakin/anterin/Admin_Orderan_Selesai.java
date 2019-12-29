package com.cetakin.anterin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

public class Admin_Orderan_Selesai extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    public PageAdapterSelesai pagerAdapter;
    private TabItem tab1s, tab2s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orderan_selesai);

        tabLayout = (TabLayout)findViewById(R.id.tab_menu_selesai);
        tab1s = (TabItem)findViewById(R.id.tab1s);
        tab2s = (TabItem)findViewById(R.id.tab2s);
        viewPager = (ViewPager)findViewById(R.id.viewpager_selesai);
        pagerAdapter = new PageAdapterSelesai(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);

        tabLayout.setOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 0){
                    pagerAdapter.notifyDataSetChanged();
                }else if (tab.getPosition() == 1){
                    pagerAdapter.notifyDataSetChanged();
                }
                /*else if (tab.getPosition() == 2){
                    pagerAdapter.notifyDataSetChanged();
                }*/
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    public void onBackPressed(){
        //kembali ke home page
        Intent back = new Intent(Admin_Orderan_Selesai.this, AdminPage.class);
        startActivity(back);
        finish();
    }
}
