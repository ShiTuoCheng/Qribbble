package stcdribbble.shituocheng.com.qribbble.UI.View;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import stcdribbble.shituocheng.com.qribbble.R;
import stcdribbble.shituocheng.com.qribbble.Utilities.AnimationUtils;

import static android.app.Activity.RESULT_OK;

/**
 * Created by shituocheng on 2016/10/10.
 */

public class BottomDialog extends DialogFragment {

    private ImageView imageView;
    private int SELECT_PICTURE = 2;
    public final static int CONSULT_DOC_PICTURE = 1000;
    private ImageButton imageButton;



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
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.create();
                dialog.setMessage("Sending...(不用等了，本功能还没完善，发不出去的)");
                dialog.show();
            }
        });
        AnimationUtils.slideToUp(view);
        return view;
    }

    private void setUpView(View view){
        imageView = (ImageView)view.findViewById(R.id.shots_create_select_image);
        imageButton = (ImageButton)view.findViewById(R.id.shots_create_send_imageButton);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONSULT_DOC_PICTURE){
            if (resultCode == RESULT_OK && data != null){
                decodeUri(data.getData());
            }
        }
    }

    public void decodeUri(Uri uri) {
        ParcelFileDescriptor parcelFD = null;
        try {
            parcelFD = getActivity().getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor imageSource = parcelFD.getFileDescriptor();

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

}