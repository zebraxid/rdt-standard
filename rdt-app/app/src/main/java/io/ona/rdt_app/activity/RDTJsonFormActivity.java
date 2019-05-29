package io.ona.rdt_app.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.vijay.jsonwizard.activities.JsonFormActivity;

import edu.washington.cs.ubicomplab.rdt_reader.ImageUtil;
import util.RDTCaptureJsonFormUtils;

import static com.vijay.jsonwizard.utils.PermissionUtils.PHONE_STATE_PERMISSION;
import static edu.washington.cs.ubicomplab.rdt_reader.Constants.REQUEST_CAMERA_PERMISSION;

public class RDTJsonFormActivity extends JsonFormActivity {

    private RDTCaptureJsonFormUtils formUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        formUtils = new RDTCaptureJsonFormUtils();
        new ImageUtil().requestCameraPermission(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.propertyManager.grantPhoneStatePermission();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Intent resultIntent = new Intent();
                setResult(RESULT_CANCELED, resultIntent);
                formUtils.showToast(this, "RDT image capture requires camera permission");
                finish();
            }
        } else if (requestCode == PHONE_STATE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeFormFragment();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                formUtils.showToast(this, "Phone state permissions are required to submit this form");
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void showPermissionDeniedDialog() {
        // do nothing
    }
}
