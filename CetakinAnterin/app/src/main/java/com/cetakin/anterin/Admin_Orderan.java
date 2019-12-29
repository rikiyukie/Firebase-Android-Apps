package com.cetakin.anterin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cetakin.anterin.Model.Order;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class Admin_Orderan extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    public PageAdapter pagerAdapter;
    private TabItem tab1admin, tab2admin, tab3admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orderan);

       tabLayout = (TabLayout)findViewById(R.id.tab_menu);
       tab1admin = (TabItem)findViewById(R.id.tab1);
       tab2admin = (TabItem)findViewById(R.id.tab2);
       tab3admin = (TabItem)findViewById(R.id.tab3);
       viewPager = (ViewPager)findViewById(R.id.viewpager);
       pagerAdapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
       viewPager.setAdapter(pagerAdapter);

       tabLayout.setOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
           @Override
           public void onTabSelected(TabLayout.Tab tab) {
               viewPager.setCurrentItem(tab.getPosition());
               if (tab.getPosition() == 0){
                   pagerAdapter.notifyDataSetChanged();
               }else if (tab.getPosition() == 1){
                   pagerAdapter.notifyDataSetChanged();
               }else if (tab.getPosition() == 2){
                   pagerAdapter.notifyDataSetChanged();
               }
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
        Intent back = new Intent(Admin_Orderan.this, AdminPage.class);
        startActivity(back);
        finish();
    }
}
