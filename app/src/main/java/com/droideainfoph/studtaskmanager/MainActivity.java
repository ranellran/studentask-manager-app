package com.droideainfoph.studtaskmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.droideainfoph.studtaskmanager.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        navigationView = binding.navView;

        // Determine the user's login role
        Intent intent = getIntent();
        String userRole = intent.getStringExtra("USER_ROLE");

        // Set the start destination based on the user's login role
        int startDestination = 0;
        if ("teacher".equals(userRole)) {
            startDestination = R.id.nav_t_dashboard;
            // Hide the student dashboard menu item
            navigationView.getMenu().findItem(R.id.nav_s_dashboard).setVisible(false);
        } else if ("student".equals(userRole)) {
            startDestination = R.id.nav_s_dashboard;
            // Hide the teacher dashboard menu item
            navigationView.getMenu().findItem(R.id.nav_t_dashboard).setVisible(false);
        }

        // Inflate the navigation graph with the updated start destination
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.mobile_navigation);
        navGraph.setStartDestination(startDestination);
        navController.setGraph(navGraph);

        mAppBarConfiguration = new AppBarConfiguration.Builder(navGraph)
                .setOpenableLayout(drawer)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Handle log-out menu item click
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Create an intent to go back to the LoginActivity
        Intent intent = new Intent(this, LogInMain.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        // Remove all other open activities from the back stack
        startActivity(intent);
        finish();
    }
}
