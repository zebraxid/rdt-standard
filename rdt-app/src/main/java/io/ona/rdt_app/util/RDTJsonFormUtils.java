package io.ona.rdt_app.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.domain.ProfileImage;
import org.smartregister.domain.UniqueId;
import org.smartregister.exception.JsonFormMissingStepCountException;
import org.smartregister.repository.ImageRepository;
import org.smartregister.util.AssetHandler;
import org.smartregister.view.activity.DrishtiApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import edu.washington.cs.ubicomplab.rdt_reader.ImageProcessor;
import edu.washington.cs.ubicomplab.rdt_reader.ImageUtil;
import edu.washington.cs.ubicomplab.rdt_reader.callback.OnImageSavedCallBack;
import io.ona.rdt_app.BuildConfig;
import io.ona.rdt_app.activity.RDTJsonFormActivity;
import io.ona.rdt_app.application.RDTApplication;
import io.ona.rdt_app.callback.OnUniqueIdFetchedCallback;
import io.ona.rdt_app.model.Patient;
import timber.log.Timber;

import static io.ona.rdt_app.util.Constants.BULLET_DOT;
import static io.ona.rdt_app.util.Constants.Form.RDT_ID;
import static io.ona.rdt_app.util.Constants.JSON_FORM_PARAM_JSON;
import static io.ona.rdt_app.util.Constants.MULTI_VERSION;
import static io.ona.rdt_app.util.Constants.REQUEST_CODE_GET_JSON;
import static org.smartregister.util.JsonFormUtils.ENTITY_ID;
import static org.smartregister.util.JsonFormUtils.KEY;
import static org.smartregister.util.JsonFormUtils.VALUE;
import static org.smartregister.util.JsonFormUtils.getMultiStepFormFields;
import static org.smartregister.util.JsonFormUtils.getString;

/**
 * Created by Vincent Karuri on 24/05/2019
 */
public class RDTJsonFormUtils {

    private static final String TAG = RDTJsonFormUtils.class.getName();

    public static void saveStaticImageToDisk(final Context context, final Bitmap image, final String providerId,
                                             final String entityId, final ImageProcessor.InterpretationResult testResult, final OnImageSavedCallBack onImageSavedCallBack) {

        if (image == null || StringUtils.isBlank(providerId) || StringUtils.isBlank(entityId)) {
            onImageSavedCallBack.onImageSaved(null);
            return;
        }

        class SaveImageTask extends AsyncTask<Void, Void, ProfileImage> {

            @Override
            protected ProfileImage doInBackground(Void... voids) {
                ProfileImage profileImage = new ProfileImage();
                OutputStream outputStream = null;
                try {
                    if (!StringUtils.isBlank(entityId)) {
                        final String imgFolderPath = DrishtiApplication.getAppDir() + File.separator + entityId;
                        final File imageFolder = new File(imgFolderPath);
                        boolean success = true;
                        if (!imageFolder.exists()) {
                            success = imageFolder.mkdirs();
                        }
                        // save captured image
                        if (success) {
                            String absoluteFilePath = imgFolderPath + File.separator + UUID.randomUUID() + ".JPEG";
                            File outputFile = new File(absoluteFilePath);

                            outputStream = new FileOutputStream(outputFile);
                            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                            // insert into db
                            profileImage.setImageid(UUID.randomUUID().toString());
                            profileImage.setAnmId(providerId);
                            profileImage.setEntityID(entityId);
                            profileImage.setFilepath(absoluteFilePath);
                            profileImage.setFilecategory(MULTI_VERSION);
                            profileImage.setSyncStatus(ImageRepository.TYPE_Unsynced);
                            ImageRepository imageRepo = RDTApplication.getInstance().getContext().imageRepository();
                            imageRepo.add(profileImage);
                            if (BuildConfig.SAVE_IMAGES_TO_GALLERY) {
                                saveImageToGallery(context, image);
                            }
                        } else {
                            Timber.e(TAG, "Sorry, could not create image folder!");
                        }

                    }
                } catch(FileNotFoundException e){
                    Timber.e(TAG, e);
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            Timber.e(TAG, e);
                        }
                    }
                }

                return profileImage;
            }

            @Override
            protected void onPostExecute(ProfileImage profileImage) {
                // todo: update this to top, middle, bottom after capture lib upgrade
                onImageSavedCallBack.onImageSaved(profileImage.getImageid()
                        + "," + System.currentTimeMillis()
                        + "," + testResult.control
                        + "," + testResult.testA
                        + "," + testResult.testB);
            }
        }

        new SaveImageTask().execute();
    }

    private static void saveImageToGallery(Context context, Bitmap image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        ImageUtil.saveImage(context, stream.toByteArray(), 0, false, new OnImageSavedCallBack() {
            @Override
            public void onImageSaved(String imageLocation) {
                // do nothing
            }
        });
    }

    public static Bitmap convertByteArrayToBitmap(byte[] src) {
        return BitmapFactory.decodeByteArray(src, 0, src.length);
    }

    public void startJsonForm(JSONObject form, Activity context, int requestCode) {
        Intent intent = new Intent(context, RDTJsonFormActivity.class);
        try {
            intent.putExtra(JSON_FORM_PARAM_JSON, form.toString());
            context.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Timber.e(TAG, e);
        }
    }

    public JSONObject getFormJsonObject(String formName, Context context) throws JSONException {
        String formString = AssetHandler.readFileFromAssetsFolder(formName, context);
        return new JSONObject(formString);
    }

    public void showToast(final Activity activity, final String text) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void launchForm(Activity activity, String formName, Patient patient, String rdtId) throws JSONException {
        try {
            JSONObject formJsonObject = getFormJsonObject(formName, activity);
            prePopulateFormFields(formJsonObject, patient, rdtId, 8);
            startJsonForm(formJsonObject, activity, REQUEST_CODE_GET_JSON);
        } catch (JsonFormMissingStepCountException e) {
            Timber.e(TAG, e);
        }
    }

    public void prePopulateFormFields(JSONObject jsonForm, Patient patient, String rdtId, int numFields) throws JSONException, JsonFormMissingStepCountException {
        jsonForm.put(ENTITY_ID, patient == null ? null : patient.getBaseEntityId());
        JSONArray fields = getMultiStepFormFields(jsonForm);
        int fieldsPopulated = 0;
        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.getJSONObject(i);
            // pre-populate rdt id labels
            if (Constants.Form.LBL_RDT_ID.equals(field.getString(KEY))) {
                field.put("text", "RDT ID: " + rdtId);
                fieldsPopulated++;
            }
            // pre-populate rdt id field
            if (RDT_ID.equals(field.getString(KEY))) {
                field.put(VALUE, rdtId);
                fieldsPopulated++;
            }
            // pre-populate patient fields
            if (patient != null) {
                if (Constants.Form.LBL_PATIENT_NAME.equals(field.getString(KEY))) {
                    field.put(VALUE, rdtId);
                    field.put("text", patient.getPatientName());
                    fieldsPopulated++;
                } else if (Constants.Form.LBL_PATIENT_GENDER_AND_ID.equals(field.getString(KEY))) {
                    field.put(VALUE, rdtId);
                    field.put("text", patient.getPatientSex() + BULLET_DOT + "ID: " + patient.getBaseEntityId());
                    fieldsPopulated++;
                }
            }
            // save cpu time
            if (fieldsPopulated == numFields) {
                break;
            }
        }
    }

    public synchronized void getNextUniqueId(final FormLaunchArgs args, final OnUniqueIdFetchedCallback callBack) {
        class FetchUniqueIdTask extends AsyncTask<Void, Void, UniqueId> {
            @Override
            protected UniqueId doInBackground(Void... voids) {
                return RDTApplication.getInstance().getContext().getUniqueIdRepository().getNextUniqueId();
            }

            @Override
            protected void onPostExecute(UniqueId result) {
                if (callBack != null) {
                    callBack.onUniqueIdFetched(args, result == null ? new UniqueId() : result);
                }
            }
        }
        new FetchUniqueIdTask().execute();
    }

    public static void appendEntityId(JSONObject jsonForm) throws JSONException {
        String entityId = getString(jsonForm, Constants.ENTITY_ID);
        entityId = entityId == null ? UUID.randomUUID().toString() : entityId;
        jsonForm.put(Constants.ENTITY_ID, entityId);
    }
}