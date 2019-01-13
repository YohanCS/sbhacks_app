package com.sbhacks.myapplication;

import android.view.Gravity;
import android.content.Context;
import android.content.ClipData;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.content.Intent;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ViewGroup.LayoutParams;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ClipboardManager;

import com.snapchat.kit.sdk.SnapKit;
import com.snapchat.kit.sdk.SnapLogin;
import com.snapchat.kit.sdk.bitmoji.OnBitmojiSearchFocusChangeListener;
import com.snapchat.kit.sdk.bitmoji.OnBitmojiSelectedListener;
import com.snapchat.kit.sdk.bitmoji.ui.BitmojiFragment;
import com.snapchat.kit.sdk.bitmoji.ui.BitmojiIconFragment;
import com.snapchat.kit.sdk.core.controller.LoginStateController;
import com.snapchat.kit.sdk.login.models.UserDataResponse;
import com.snapchat.kit.sdk.login.networking.FetchUserDataCallback;


public class BitmojiActivity extends AppCompatActivity implements OnBitmojiSelectedListener{
    private View mContentView;
    private View mBitmojiContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitmoji);
        Toast.makeText(this, "Click on a tag to save it to clipboard.",Toast.LENGTH_LONG).show();

        TextView current;
        LinearLayout layout = (LinearLayout)findViewById(R.id.text_views);
        current = new TextView(this);
        current.setText("Detected Features: \n");
        current.setTextSize(32);
        current.setGravity(Gravity.CENTER_HORIZONTAL);
        current.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        layout.addView(current);






        for (String E : MainActivity.features) {
            current = new TextView(this);
            current.setText(E+"\n");
            current.setTextSize(22);
            current.setGravity(Gravity.CENTER_HORIZONTAL);
            current.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));

            current.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Copied Text", E);
                    clipboard.setPrimaryClip(clip);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.sdk_container, new BitmojiFragment(), "ggg")
                            .commit();

                    //MainActivity.reset = true;
                    Intent intent = new Intent(BitmojiActivity.this, BitmojiSelectActivity.class);
                    startActivity(intent);


                }
            });
            layout.addView(current);
        }
            //(BitmojiFragment) getSupportFragmentManager().findFragmentById(R.id.sdk_orig);


        //fragment.setSearchText("water");

        //

//        this.getSupportFragmentManager().recreateFragment(fragment).commit();

        /*getSupportFragmentManager().beginTransaction()
                .replace(R.id.sdk_container, new BitmojiFragment(), "ggg")
                .commit();*/
    }

    @Override
    public void onBitmojiSelected(String imageUrl, Drawable previewDrawable) {
        Toast.makeText(this,"clicked on a bitmoji",Toast.LENGTH_SHORT).show();
    }

}





/*import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import android.support.v4.app.Fragment;

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
import java.util.Locale;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.util.Log;
import android.widget.Toast;


import com.snapchat.kit.sdk.bitmoji.ui.BitmojiFragment;


public class BitmojiActivity extends AppCompatActivity implements OnBitmojiSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitmoji);

        loadBitmojis("doesn't query yet..");




    }
    public void loadBitmojis(String s)
    {

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bitmoji_container, BitmojiFragment.builder()
                        .withShowSearchBar(true)
                        .withShowSearchPills(true)
                        .build())
                .commit();
        Toast.makeText(this, s,Toast.LENGTH_LONG).show();

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.bitmoji_container);
        if (fragment instanceof BitmojiFragment) {
            ((BitmojiFragment) fragment).setSearchText("water");
            Toast.makeText(this,"set search text",Toast.LENGTH_LONG).show();
        }
        else
            ;//Toast.makeText(this,"fragment was null",Toast.LENGTH_SHORT).show();


    }



    }

    @Override
    public void onBitmojiSelected(String imageUrl, Drawable previewDrawable) {
        Toast.makeText(getApplicationContext(),"clicked on a bitmoji",Toast.LENGTH_SHORT).show();
        //handleBitmojiSend(imageUrl, previewDrawable);
    }


    public BitmojiFragment getActiveFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            return null;
        }
        String tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
        return (BitmojiFragment) getSupportFragmentManager().findFragmentByTag(tag);
    }

}
*/