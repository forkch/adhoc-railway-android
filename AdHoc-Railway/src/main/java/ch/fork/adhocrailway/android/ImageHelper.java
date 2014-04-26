package ch.fork.adhocrailway.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by fork on 4/16/14.
 */
public class ImageHelper {
    public static void fillImageViewFromBase64ImageString(ImageView imageView, String imageBase64) {

        if(StringUtils.isNotBlank(imageBase64)) {
            byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            imageView.setImageBitmap(decodedByte);
        }
    }
}
