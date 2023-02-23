package it.unibo.cs.lam2021.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import it.unibo.cs.lam2021.R;
import it.unibo.cs.lam2021.api.ApiService;
import it.unibo.cs.lam2021.api.User;
import it.unibo.cs.lam2021.databinding.ActivityMainBinding;
import it.unibo.cs.lam2021.preferences.ApplicationPreferences;
import it.unibo.cs.lam2021.preferences.EncryptedPreferences;
import it.unibo.cs.lam2021.ui.login.LoginActivity;
import it.unibo.cs.lam2021.ui.scanner.ScannerActivity;
import it.unibo.cs.lam2021.ui.settings.SettingsActivity;
import retrofit2.HttpException;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private boolean FABExpanded = false;

    private ApplicationPreferences applicationPreferences;
    private EncryptedPreferences encryptedPreferences;

    private TextView loggedInUsername;
    private TextView loggedInEmail;

    private SwipeRefreshLayout swipeRefresh;

    boolean backPressed = false;

    /* to avoid stuttering, wait for drawer close animation before doing things */
    private static class DrawerCloseAction implements DrawerLayout.DrawerListener {

        public interface Listener {
            void onCloseDrawer();
        }

        private final DrawerLayout drawer;
        private final Listener listener;

        public DrawerCloseAction(DrawerLayout drawer, Listener listener) {
            this.drawer = drawer;
            this.listener = listener;
        }

        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) { }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            listener.onCloseDrawer();
            drawer.removeDrawerListener(this);
        }

        @Override
        public void onDrawerStateChanged(int newState) { }
    }

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onLost(Network network) {
            applicationPreferences.setNetworkOnline(false);
        }

        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            //prevent showing loading animation if network is already online
            if (applicationPreferences.isNetworkOnline())
                return;

            runOnUiThread(() -> {
                swipeRefresh.setRefreshing(true);
            });

            onRefresh();
        }
    };

    private final SharedPreferences.OnSharedPreferenceChangeListener networkChangedListener = (sharedPreferences, s) -> {
        if(s.equals(ApplicationPreferences.NETWORK_ONLINE)) {
            if(sharedPreferences.getBoolean(ApplicationPreferences.NETWORK_ONLINE, false))
                setOnline();
            else
                setOffline();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applicationPreferences = ApplicationPreferences.getInstance(this);
        encryptedPreferences = EncryptedPreferences.getInstance(this);

        setSupportActionBar(binding.appBarMain.toolbar);

        binding.appBarMain.fab.setOnClickListener(view -> {
            if(FABExpanded) {
                collapseFABMenu();
            } else {
                expandFABMenu();
            }
        });

        binding.appBarMain.addCamera.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, ScannerActivity.class)));
        binding.appBarMain.addManual.setOnClickListener(view ->
                new ManualAddDialogFragment().show(getSupportFragmentManager(), ManualAddDialogFragment.TAG));

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_pantry, R.id.nav_categories)
                .setOpenableLayout(binding.drawerLayout)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        binding.navView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_logout:
                    binding.drawerLayout.closeDrawer(GravityCompat.START, true);
                    binding.drawerLayout.addDrawerListener(new DrawerCloseAction(binding.drawerLayout, () ->
                        new LogoutConfirmDialogFragment().show(getSupportFragmentManager(), LogoutConfirmDialogFragment.TAG)
                    ));
                    return false;

                case R.id.nav_settings:
                    binding.drawerLayout.closeDrawer(GravityCompat.START, true);
                    binding.drawerLayout.addDrawerListener(new DrawerCloseAction(binding.drawerLayout, () ->
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class))
                    ));
                    return false;
            }

            navController.navigate(item.getItemId());
            getSupportFragmentManager().executePendingTransactions();
            binding.drawerLayout.closeDrawer(GravityCompat.START, true);
            return true;
        });

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(this);

        loggedInUsername = binding.navView.getHeaderView(0).findViewById(R.id.logged_in_user);
        loggedInEmail = binding.navView.getHeaderView(0).findViewById(R.id.logged_in_email);

        if(applicationPreferences.isNetworkOnline())
            setOnline();
        else
            setOffline();

        applicationPreferences.getPreferences()
                .registerOnSharedPreferenceChangeListener(networkChangedListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);

        Network network = connectivityManager.getActiveNetwork();
        if(network != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
                applicationPreferences.setNetworkOnline(false);
        } else
            applicationPreferences.setNetworkOnline(false);

        connectivityManager.registerNetworkCallback(
                new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                        .build(),
                networkCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onRefresh() {
        String email = encryptedPreferences.getEmail();
        String password = encryptedPreferences.getPassword();
        ListenableFuture<User> userFuture = ApiService.getInstance(this).login(email, password);
        userFuture.addListener(() -> {
            try {
                User user = userFuture.get();
                encryptedPreferences.setUsername(user.getUsername())
                        .setUserId(user.getId());
                applicationPreferences.setNetworkOnline(true);
                swipeRefresh.setRefreshing(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                Throwable t = e.getCause();
                if (t instanceof ApiService.InvalidCredentialsException) {
                    Toast.makeText(getApplicationContext(), R.string.error_stored_credentials, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                } else if (t instanceof HttpException) {
                    applicationPreferences.setNetworkOnline(true);

                    int errorCode = ((HttpException) t).code();
                    Toast.makeText(this, getString(R.string.error_http, errorCode), Toast.LENGTH_LONG).show();

                    swipeRefresh.setRefreshing(false);
                } else
                    swipeRefresh.setRefreshing(false);
            }
        }, ContextCompat.getMainExecutor(getApplicationContext()));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(FABExpanded) {
            if(ev.getAction() == MotionEvent.ACTION_DOWN) {
                Rect outRect = new Rect();
                Rect manualRect = new Rect();
                binding.appBarMain.fab.getGlobalVisibleRect(outRect);
                binding.appBarMain.addManual.getGlobalVisibleRect(manualRect);
                outRect.union(manualRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY()))
                    collapseFABMenu();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void expandFABMenu() {
        FABExpanded = true;

        binding.appBarMain.addCamera.animate().translationY(-getResources().getDimensionPixelSize(R.dimen.fab_transition1));
        binding.appBarMain.addManual.animate().translationY(-getResources().getDimensionPixelSize(R.dimen.fab_transition2));
        binding.appBarMain.addCamera.setElevation(binding.appBarMain.fab.getElevation());
        binding.appBarMain.addManual.setElevation(binding.appBarMain.fab.getElevation());
    }

    public void collapseFABMenu() {
        FABExpanded = false;
        binding.appBarMain.addCamera.animate().translationY(0);
        binding.appBarMain.addManual.animate().translationY(0);
        binding.appBarMain.addCamera.setElevation(0);
        binding.appBarMain.addManual.setElevation(0);
    }

    public void setOffline() {
        binding.appBarMain.offlineAlert.setText(R.string.offline_alert);
        binding.appBarMain.offlineAlert.setActivated(false);
        binding.appBarMain.offlineAlert.setVisibility(View.VISIBLE);

        swipeRefresh.setRefreshing(false);
        swipeRefresh.setEnabled(true);

        binding.appBarMain.fab.setVisibility(View.GONE);
        binding.appBarMain.addCamera.setVisibility(View.GONE);
        binding.appBarMain.addManual.setVisibility(View.GONE);

        binding.navView.getMenu().findItem(R.id.nav_logout).setVisible(false);

        loggedInUsername.setText("");
        loggedInEmail.setText("");
    }

    public void setOnline() {
        binding.appBarMain.offlineAlert.setVisibility(View.GONE);

        swipeRefresh.setRefreshing(false);
        swipeRefresh.setEnabled(false);

        binding.appBarMain.fab.setVisibility(View.VISIBLE);
        binding.appBarMain.addCamera.setVisibility(View.VISIBLE);
        binding.appBarMain.addManual.setVisibility(View.VISIBLE);

        binding.navView.getMenu().findItem(R.id.nav_logout).setVisible(true);

        loggedInUsername.setText(EncryptedPreferences.getInstance(this).getUsername());
        loggedInEmail.setText(EncryptedPreferences.getInstance(this).getEmail());
    }

    @Override
    public void onBackPressed() {
        MenuItem searchItem = binding.appBarMain.toolbar.getMenu().findItem(R.id.app_bar_search);

        if(searchItem != null) {
            SearchView sv = (SearchView) binding.appBarMain.toolbar.getMenu().findItem(R.id.app_bar_search).getActionView();

            if(!sv.isIconified()) {
                sv.setIconified(true);
                return;
            }
        }

        if(FABExpanded) {
            collapseFABMenu();
            return;
        }

        if(backPressed)
            finish();
        else {
            backPressed = true;
            Toast.makeText(this, R.string.press_back_again, Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> backPressed = false, 2000);
        }
    }
}