package stcdribbble.shituocheng.com.qribbble.UI.View;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.Utilities.API;
import stcdribbble.shituocheng.com.qribbble.Utilities.Access_Token;
import stcdribbble.shituocheng.com.qribbble.Utilities.AnimationUtils;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by shituocheng on 2016/10/10.
 */

public class BottomDialog extends DialogFragment {

    private ImageView imageView;
    private static final int MSG_WHAT_RESP = 1;
    public final static int CONSULT_DOC_PICTURE = 1000;
    private ImageButton imageButton;
    private TextInputEditText title_textInput;
    private TextInputEditText description_textInput;
    private TextInputEditText tags_textInput;
    private Handler handler;



    public static BottomDialog newInstance() {

        Bundle args = new Bundle();

        BottomDialog fragment = new BottomDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.bottom_dialog_view,container,false);
        setUpView(view);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Choose an image"), CONSULT_DOC_PICTURE);
            }
        });

        AnimationUtils.slideToUp(view);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case MSG_WHAT_RESP:
                        int code = (int) msg.obj;
                        if (code != 202 ){
                            Toast.makeText(getActivity(), "Failed to post", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getActivity(), "post successfully!!", Toast.LENGTH_SHORT).show();
                        }
                }
            }
        };
        return view;
    }

    private void setUpView(View view){
        imageView = (ImageView)view.findViewById(R.id.shots_create_select_image);
        imageButton = (ImageButton)view.findViewById(R.id.shots_create_send_imageButton);
        title_textInput = (TextInputEditText)view.findViewById(R.id.shots_create_title_editText);
        description_textInput = (TextInputEditText)view.findViewById(R.id.shots_create_description_editText);
        tags_textInput = (TextInputEditText)view.findViewById(R.id.shots_create_tags_editText);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == CONSULT_DOC_PICTURE){
            if (resultCode == RESULT_OK && data != null){
                decodeUri(data.getData());
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ProgressDialog dialog = new ProgressDialog(getActivity());
                        dialog.create();
                        dialog.setMessage("Sending...(不用等了，本功能还没完善，发不出去的)");
                        upLoadFile(data.getData());
                        dialog.show();
                    }
                });
            }
        }
    }

    public void upLoadFile(Uri uri){

        File file = new File(String.valueOf(uri));
        String title = title_textInput.getText().toString();
        String description = description_textInput.getText().toString();
        String tags = tags_textInput.getText().toString();

        //check isLogin

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_login_data",MODE_PRIVATE);
        final String access_token = sharedPreferences.getString("access_token","");

        if (title.isEmpty()){

            Toast.makeText(getActivity(), "Title is required", Toast.LENGTH_SHORT).show();

        }else if (access_token.isEmpty()){
            Toast.makeText(getActivity(), getResources().getText(R.string.login_your_account), Toast.LENGTH_SHORT).show();
        }else {
            HttpURLConnection connection;
            String api = API.generic_api+"shots?access_token="+ Access_Token.access_token;

            try {
                connection = (HttpURLConnection)new URL(api).openConnection();
                connection.setRequestMethod("POST");
                connection.connect();

                PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
                printWriter.print(title);
                if (!description.isEmpty()){
                    printWriter.print(description);
                }else if (!tags.isEmpty()){
                    printWriter.print(tags);
                }
                printWriter.print(file);

                printWriter.flush();
                printWriter.close();

                int code = connection.getResponseCode();

                Message message = handler.obtainMessage();
                message.what = MSG_WHAT_RESP;
                message.obj = code;
                message.sendToTarget();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }

    public void decodeUri(Uri uri) {
        ParcelFileDescriptor parcelFD = null;
        try {
            parcelFD = getActivity().getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor imageSource = parcelFD != null ? parcelFD.getFileDescriptor() : null;

            Log.d("URI", String.valueOf(uri));

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(imageSource, null, o);

            // the new size we want to scale to
            final int REQUIRED_SIZE = 1024;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(imageSource, null, o2);

            imageView.setImageBitmap(bitmap);

            File file = new File(String.valueOf(uri));

        } catch (FileNotFoundException e) {
            // handle errors
        } finally {
            if (parcelFD != null)
                try {
                    parcelFD.close();
                } catch (IOException e) {
                    // ignored
                }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}