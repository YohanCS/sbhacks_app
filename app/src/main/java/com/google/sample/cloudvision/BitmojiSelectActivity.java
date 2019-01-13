package com.sbhacks.myapplication;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;

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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.annotation.*;
import java.text.MessageFormat;
import java.util.*;
import java.net.*;
import java.io.*;

import static java.security.AccessController.getContext;


public class BitmojiSelectActivity extends AppCompatActivity implements OnBitmojiSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitmoji_select);
        Toast.makeText(this, "Press and hold on the search bar and paste.",Toast.LENGTH_LONG).show();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.sdk_container, new BitmojiFragment(), "ggg")
                .commit();


    }
    @Override
    public void onBitmojiSelected(String s, Drawable previewDrawable) {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_SUBJECT, "BitPic");
            send.putExtra(Intent.EXTRA_TEXT, s);
            startActivity(Intent.createChooser(send, ""));
    }

}
