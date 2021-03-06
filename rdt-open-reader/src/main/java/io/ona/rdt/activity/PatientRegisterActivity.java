package io.ona.rdt.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.json.JSONObject;
import org.smartregister.domain.FetchStatus;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.ArrayList;
import java.util.List;

import io.ona.rdt.R;
import io.ona.rdt.application.RDTApplication;
import io.ona.rdt.callback.OnFormSavedCallback;
import io.ona.rdt.contract.PatientRegisterActivityContract;
import io.ona.rdt.fragment.PatientRegisterFragment;
import io.ona.rdt.presenter.PatientRegisterActivityPresenter;
import io.ona.rdt.util.RDTJsonFormUtils;
import io.ona.rdt.util.Utils;
import timber.log.Timber;

import static io.ona.rdt.util.Constants.Config.IS_IMG_SYNC_ENABLED;
import static io.ona.rdt.util.Constants.RequestCodes.REQUEST_CODE_GET_JSON;
import static io.ona.rdt.util.Constants.RequestCodes.REQUEST_RDT_PERMISSIONS;
import static io.ona.rdt.util.Utils.updateLocale;

public class PatientRegisterActivity extends BaseRegisterActivity implements SyncStatusBroadcastReceiver.SyncStatusListener, OnFormSavedCallback, PatientRegisterActivityContract.View {

    private DrawerLayout drawerLayout;
    private final String TAG = PatientRegisterActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        updateLocale(this);
        super.onCreate(savedInstanceState);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        setupDrawerContent(navigationView);
        requestPermissions();
    }

    public void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_RDT_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RDT_PERMISSIONS && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            new RDTJsonFormUtils().showToast(this, getApplicationContext().getString(R.string.rdt_permissions_required));
            finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void initializePresenter() {
        presenter = new PatientRegisterActivityPresenter(this);
    }

    @Override
    public BaseRegisterFragment getRegisterFragment() {
        return new PatientRegisterFragment();
    }


    @Override
    protected android.support.v4.app.Fragment[] getOtherFragments() {
        return new android.support.v4.app.Fragment[0];
    }

    @Override
    public void startFormActivity(String s, String s1, String s2) {
        // TODO: implement this
    }

    @Override
    public void startFormActivity(JSONObject jsonObject) {
        // TODO: implement this
    }

    @Override
    protected void onActivityResultExtended(int i, int i1, Intent intent) {
        // TODO: implement this
    }

    @Override
    public List<String> getViewIdentifiers() {
        return new ArrayList<>();         // TODO: implement this
    }

    @Override
    public void startRegistration() {
        // do this in register fragment
    }

    @Override
    protected void registerBottomNavigation() {
        //not used for task register
        findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
    }

    @Override
    public void onSyncStart() {
        // TODO: implement this
    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        // TODO: implement this
    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        // TODO: implement this
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_GET_JSON && resultCode == Activity.RESULT_OK && data != null) {
            String jsonForm = data.getStringExtra("json");
            Timber.d(TAG, jsonForm);
            getPresenter().saveForm(jsonForm, this);
        }
    }

    @Override
    public void onFormSaved() {
        if (mBaseFragment != null && mBaseFragment.getActivity() != null) {
            mBaseFragment.refreshListView();
        }
    }

    public void openDrawerLayout() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void closeDrawerLayout() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        Menu menuNav = navigationView.getMenu();
        MenuItem imgSyncToggle = menuNav.findItem(R.id.menu_item_toggle_img_sync);
        View actionView = imgSyncToggle.getActionView();
        Switch imgSyncToggleBtn = actionView.findViewById(R.id.img_sync_switch_button);
        imgSyncToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                RDTApplication.getInstance().getContext()
                        .allSharedPreferences()
                        .savePreference(IS_IMG_SYNC_ENABLED, String.valueOf(!Utils.isImageSyncEnabled()));
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                selectDrawerItem(menuItem);
                return true;
            }
        });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.menu_item_sync:
                closeDrawerLayout();
                Utils.scheduleJobsImmediately();
                break;
            case R.id.menu_item_logout:
                RDTApplication.getInstance().logoutCurrentUser();
                break;
            default:
                // do nothing
        }
    }

    private PatientRegisterActivityPresenter getPresenter() {
        return (PatientRegisterActivityPresenter) this.presenter;
    }
}
