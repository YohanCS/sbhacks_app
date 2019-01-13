/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sbhacks.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;

import android.graphics.drawable.Drawable;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

// Import needed for LoginStateController
import com.snapchat.kit.sdk.Bitmoji;
import com.snapchat.kit.sdk.bitmoji.OnBitmojiSelectedListener;
import com.snapchat.kit.sdk.bitmoji.networking.FetchAvatarUrlCallback;
import com.snapchat.kit.sdk.bitmoji.ui.BitmojiFragment;
import com.snapchat.kit.sdk.bitmoji.ui.BitmojiIconFragment;
import com.snapchat.kit.sdk.core.controller.LoginStateController;
import com.snapchat.kit.sdk.SnapLogin;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.snapchat.kit.sdk.login.models.MeData;
import com.snapchat.kit.sdk.login.models.UserDataResponse;
import com.snapchat.kit.sdk.login.networking.FetchUserDataCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Locale;


import static java.security.AccessController.getContext;


public class MainActivity extends AppCompatActivity implements OnBitmojiSelectedListener {
    private static final String CLOUD_VISION_API_KEY = BuildConfig.API_KEY;
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;
    private final double MIN_CERTAINTY = .5;



    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    private ProgressDialog mProgressDialog;
    private String mMyExternalId;
    private TextView mImageDetails;


    public static ArrayList<String> features;
    public static ArrayList<Double> percentCertainties;

    public static boolean reset = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View mLoginButton;


        if(!SnapLogin.isUserLoggedIn(getApplicationContext()))
            mLoginButton = SnapLogin.getButton(getBaseContext(), (ViewGroup)((ViewGroup )this.findViewById(android.R.id.content)).getChildAt(0));
        else{showPrompt();
        }

        final LoginStateController.OnLoginStateChangedListener mLoginStateChangedListener =
                new LoginStateController.OnLoginStateChangedListener() {
                    @Override
                    public void onLoginSucceeded() {
                        // Here you could update UI to show login success

                        System.out.println("login succ");
                        Toast.makeText(getApplicationContext(),"successfully logged in",Toast.LENGTH_SHORT).show();
                        showPrompt();

                    }

                    @Override
                    public void onLoginFailed() {
                        // Here you could update UI to show login failure
                        System.out.println("login failed");
                        Toast.makeText(getApplicationContext(),"login failed...",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onLogout() {
                        Toast.makeText(getApplicationContext(),"please restart the app",Toast.LENGTH_SHORT).show();
                        // Here you could update UI to reflect logged out state
                    }
                };
        // Add the LoginStateChangedListener you’ve defined to receive LoginInState updates
        //SnapLogin.getLoginStateController(getApplicationContext()).addOnLoginStateListener(mLoginStateChangedListener);
        SnapLogin.getLoginStateController(getApplicationContext()).addOnLoginStateChangedListener(mLoginStateChangedListener);

        //  to fetch data
        String query = "{me{bitmoji{avatar},displayName}}";
        String variables = null;
        /* SnapLogin.fetchUserData(this, query, null, new FetchUserDataCallback() {
            @Override
            public void onSuccess(@Nullable UserDataResponse userDataResponse) {
                if (userDataResponse == null || userDataResponse.getData() == null) {
                    return;
                }


                MeData meData = userDataResponse.getData().getMe();
                if (meData == null) {
                    return;
                }

                //mNameTextView.setText(userDataResponse.getData().getMe().getDisplayName());

                if (meData.getBitmojiData() != null) {
                    /*Glide.with(getContext())
                            .load(meData.getBitmojiData().getAvatar())
                            .into(mBitmojiImageView);
                }
            }

            @Override
            public void onFailure(boolean isNetworkError, int statusCode) {

            }
        }); */



        //other code not part of snapkit
        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder
                    .setMessage(R.string.dialog_select_prompt)
                    .setPositiveButton(R.string.dialog_select_gallery, (dialog, which) -> startGalleryChooser())
                    .setNegativeButton(R.string.dialog_select_camera, (dialog, which) -> startCamera());
            builder.create().show();
        });*/
        loadExternalId();
    }


    private void loadExternalId() {
        SnapLogin.fetchUserData(this, "{me{externalId}}", null, new FetchUserDataCallback() {
            @Override
            public void onSuccess(@Nullable UserDataResponse userDataResponse) {
                if (userDataResponse == null || userDataResponse.hasError()) {
                    return;
                }
                mMyExternalId = userDataResponse.getData().getMe().getExternalId();
            }

            @Override
            public void onFailure(boolean isNetworkError, int statusCode) {
                // handle error
            }
        });
    }

    @Override
    public void onBitmojiSelected(String imageUrl, Drawable previewDrawable) {
        Toast.makeText(getApplicationContext(),"clicked on a bitmoji",Toast.LENGTH_SHORT).show();
        //handleBitmojiSend(imageUrl, previewDrawable);
    }



    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);


                // from https://stackoverflow.com/questions/35079083/android-loading-circle-spinner-between-two-activity
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setTitle(R.string.loading_message);
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();


                callCloudVision(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("LABEL_DETECTION");
                labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<MainActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(MainActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }


        //calls the bitmoji activity
        protected void onPostExecute(String result) {
            MainActivity activity = mActivityWeakReference.get();

            if (activity != null && !activity.isFinishing()) {
                Intent intent = new Intent(activity, BitmojiActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean("featDialog", false);
                intent.putExtras(bundle);
                storeResults(result);
                Log.v("data", result);
                mProgressDialog.dismiss();
                startActivity(intent);
            }
        }



    }

    private void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading

        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(this, prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder();

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message.append(String.format(Locale.US, "%.4f: %s", label.getScore(), label.getDescription()));
                message.append("\n");
            }
        } else {
            message.append("nothing: 0.0\n");
        }

        return message.toString();
    }




    private void storeResults(String response){
        String[] splitResultsArray = (String[])response.split(": ");
        ArrayList<String> splitResults = new ArrayList<>();

        try {
            for (String s1 : splitResultsArray)
                splitResults.addAll(Arrays.asList( s1.split("\n")));
        }catch (Exception e){Toast.makeText(this,"Error receiving response \uD83D\uDE22",Toast.LENGTH_SHORT).show(); throw new RuntimeException("Exception: malformed response");}

        features = new ArrayList<>();
        percentCertainties = new ArrayList<>();

        for(int i=0; i<splitResults.size(); i++){
            // even results are strings, odd results are features.
            Log.e("res","result["+i+"]: "+splitResults.get(i));
            if(i%2==0) if(Double.parseDouble( splitResults.get(i))>=MIN_CERTAINTY) percentCertainties.add(100.0*Double.parseDouble(splitResults.get(i))); else i++;
            else features.add(splitResults.get(i));
        }

        if(features.size()!=percentCertainties.size()){Toast.makeText(this,"Error receiving response \uD83D\uDE22",Toast.LENGTH_SHORT).show(); throw new RuntimeException("Exception: malformed response");}

        if(features.size()==0) {
            features.add(splitResults.get(0));
            percentCertainties.add(Double.parseDouble( splitResults.get(1)));
            Toast.makeText(this, "Your image didn't have any good matches, but here's the best guess.", Toast.LENGTH_SHORT).show();
        }
        Log.v("split","features: " + features);
        Log.v("split","pcts: " + percentCertainties);
    }

    public void showPrompt(){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(R.string.dialog_select_prompt)
                .setMessage("")
                .setPositiveButton(R.string.dialog_select_gallery, (dialog, which) -> startGalleryChooser())
                .setNegativeButton(R.string.dialog_select_camera, (dialog, which) -> startCamera())
                .setCancelable(false)
                .create().show();

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_UP && SnapLogin.isUserLoggedIn(getApplicationContext()))
            showPrompt();
        return true;
    }

    //public static boolean hasReset(){return reset;}





}
