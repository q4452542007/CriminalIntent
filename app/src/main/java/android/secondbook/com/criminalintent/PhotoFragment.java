package android.secondbook.com.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Administrator on 2017/8/31.
 */

public class PhotoFragment extends DialogFragment {

//    private static File photoFile;
    private ImageView photoView;

    private static final String ARG_PHOTOPATH= "path";


    public static PhotoFragment newInstance(String path) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PHOTOPATH, path);
        PhotoFragment fragment = new PhotoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String path = getArguments().getString("path");
         //创建一个dialog
        final Dialog dialog = new Dialog(getActivity());
        //设置dialog的布局,为之前创建的布局文件,里面仅有一个ImageView
        dialog.setContentView(R.layout.dialog_photo);
        //找到控件
        ImageView imageView = (ImageView)dialog.findViewById(R.id.photo_detail);
        //使用 PictureUtils 类的工具来获得缩放的 Bitmap
        imageView.setImageBitmap(PictureUtils.getScaledBitmap(path,getActivity()));
        //设置点击事件，当点击图片时候，dialog消失。
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                                 dialog.dismiss();
            }
         });

                 return dialog;
    }
}
