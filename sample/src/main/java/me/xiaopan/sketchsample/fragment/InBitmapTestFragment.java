package me.xiaopan.sketchsample.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import me.xiaopan.androidinjector.InjectContentView;
import me.xiaopan.androidinjector.InjectView;
import me.xiaopan.sketch.Configuration;
import me.xiaopan.sketch.Sketch;
import me.xiaopan.sketch.SketchMonitor;
import me.xiaopan.sketch.cache.BitmapPoolUtils;
import me.xiaopan.sketch.request.UriScheme;
import me.xiaopan.sketch.util.SketchUtils;
import me.xiaopan.sketchsample.AssetImage;
import me.xiaopan.sketchsample.MyFragment;
import me.xiaopan.sketchsample.R;

@InjectContentView(R.layout.fragment_in_bitmap_test)
public class InBitmapTestFragment extends MyFragment {

    @InjectView(R.id.image_inBitmapTestFragment)
    ImageView imageView;

    @InjectView(R.id.text_inBitmapTestFragment)
    TextView textView;

    @InjectView(R.id.button_inBitmapTestFragment_sizeSame)
    Button sizeSameButton;

    @InjectView(R.id.button_inBitmapTestFragment_largeSize)
    Button largeSizeButton;

    @InjectView(R.id.button_inBitmapTestFragment_sizeNoSame)
    Button sizeNoSameButton;

    @InjectView(R.id.button_inBitmapTestFragment_inSampleSize)
    Button inSampleSizeButton;

    @InjectView(R.id.view_inBitmapTestFragment_pageNumber)
    TextView pageNumberTextView;

    @InjectView(R.id.view_inBitmapTestFragment_last)
    View lastView;

    @InjectView(R.id.view_inBitmapTestFragment_next)
    View nextView;

    int index = 0;

    Configuration configuration;

    private View currentMode;

    private static Bitmap decodeImage(Context context, String imageUri, BitmapFactory.Options options) {
        UriScheme uriScheme = UriScheme.valueOfUri(imageUri);
        if (uriScheme == null) {
            return null;
        }

        InputStream inputStream;
        if (uriScheme == UriScheme.FILE) {
            try {
                inputStream = new FileInputStream(imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        } else if (uriScheme == UriScheme.ASSET) {
            try {
                inputStream = context.getAssets().open(UriScheme.ASSET.crop(imageUri));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else if (uriScheme == UriScheme.DRAWABLE) {
            try {
                inputStream = context.getResources().openRawResource(Integer.valueOf(UriScheme.DRAWABLE.crop(imageUri)));
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }

        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        } finally {
            SketchUtils.close(inputStream);
        }

        return bitmap;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configuration = Sketch.with(getActivity()).getConfiguration();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lastView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                --index;
                if (index < 0) {
                    index = AssetImage.IN_BITMAP_SAMPLES.length - Math.abs(index);
                }
                currentMode.performClick();
            }
        });

        nextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index = ++index % AssetImage.IN_BITMAP_SAMPLES.length;
                currentMode.performClick();
            }
        });

        sizeSameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testSizeSame();
                updateCheckedStatus(v);
            }
        });

        largeSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testLargeSize();
                updateCheckedStatus(v);
            }
        });

        sizeNoSameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testSizeNoSame();
                updateCheckedStatus(v);
            }
        });

        inSampleSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inSampleSize();
                updateCheckedStatus(v);
            }
        });

        sizeSameButton.performClick();
    }

    private void updateCheckedStatus(View newView) {
        if (currentMode != null) {
            currentMode.setEnabled(true);
        }

        newView.setEnabled(false);
        currentMode = newView;

        pageNumberTextView.setText(String.format("%d/%d", index + 1, AssetImage.IN_BITMAP_SAMPLES.length));
    }

    private void testSizeSame() {
        new TestTask(getActivity()) {
            @Override
            protected void configOptions(BitmapFactory.Options options) {
                if (BitmapPoolUtils.sdkSupportInBitmap()) {
                    options.inBitmap = Bitmap.createBitmap(options.outWidth, options.outHeight, options.inPreferredConfig);
                    options.inMutable = true;
                }
                super.configOptions(options);
            }
        }.execute(AssetImage.IN_BITMAP_SAMPLES[index % AssetImage.IN_BITMAP_SAMPLES.length]);
    }

    private void testLargeSize() {
        new TestTask(getActivity()) {
            @Override
            protected void configOptions(BitmapFactory.Options options) {
                if (BitmapPoolUtils.sdkSupportInBitmap()) {
                    options.inBitmap = Bitmap.createBitmap(options.outWidth + 10, options.outHeight + 5, options.inPreferredConfig);
                    options.inMutable = true;
                }
                super.configOptions(options);
            }
        }.execute(AssetImage.IN_BITMAP_SAMPLES[index % AssetImage.IN_BITMAP_SAMPLES.length]);
    }

    private void testSizeNoSame() {
        new TestTask(getActivity()) {
            @Override
            protected void configOptions(BitmapFactory.Options options) {
                if (BitmapPoolUtils.sdkSupportInBitmap()) {
                    options.inBitmap = Bitmap.createBitmap(options.outHeight, options.outWidth, options.inPreferredConfig);
                    options.inMutable = true;
                }
                super.configOptions(options);
            }
        }.execute(AssetImage.IN_BITMAP_SAMPLES[index % AssetImage.IN_BITMAP_SAMPLES.length]);
    }

    private void inSampleSize() {
        new TestTask(getActivity()) {
            @Override
            protected void configOptions(BitmapFactory.Options options) {
                if (BitmapPoolUtils.sdkSupportInBitmap()) {
                    options.inSampleSize = 2;
                    int finalWidth = SketchUtils.ceil(options.outWidth, options.inSampleSize);
                    int finalHeight = SketchUtils.ceil(options.outHeight, options.inSampleSize);
                    options.inBitmap = Bitmap.createBitmap(finalWidth, finalHeight, options.inPreferredConfig);
                    options.inMutable = true;
                }
                super.configOptions(options);
            }
        }.execute(AssetImage.IN_BITMAP_SAMPLES[index % AssetImage.IN_BITMAP_SAMPLES.length]);
    }

    private class TestTask extends AsyncTask<String, Integer, Bitmap> {
        protected StringBuilder builder = new StringBuilder();
        private Context context;

        public TestTask(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String imageUri = params[0];

            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inJustDecodeBounds = true;
            decodeImage(context, imageUri, options);

            if (options.outWidth <= 1 || options.outHeight <= 1) {
                return null;
            }

            options.inSampleSize = 1;   // 这很重要4.4以下必须得是1
            configOptions(options);

            builder.append("imageUri: ").append(imageUri);

            int sizeInBytes = SketchUtils.computeNeedByteSize(options.outWidth, options.outHeight, options.inPreferredConfig);
            builder.append("\n").append("image: ")
                    .append(options.outWidth).append("x").append(options.outHeight)
                    .append(", ").append(options.inPreferredConfig)
                    .append(", ").append(sizeInBytes);

            builder.append("\n").append("inSampleSize: ").append(options.inSampleSize);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if (options.inBitmap != null) {
                    builder.append("\n")
                            .append("inBitmap: ")
                            .append(Integer.toHexString(options.inBitmap.hashCode()))
                            .append(", ").append(options.inBitmap.getWidth()).append("x").append(options.inBitmap.getHeight())
                            .append(", ").append(options.inBitmap.isMutable())
                            .append(", ").append(SketchUtils.getBitmapByteSize(options.inBitmap));
                } else {
                    builder.append("\n").append("inBitmap: ").append("null");
                }
            }

            Bitmap newBitmap = null;
            try {
                options.inJustDecodeBounds = false;
                newBitmap = decodeImage(context, imageUri, options);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();

                SketchMonitor monitor = Sketch.with(getActivity()).getConfiguration().getMonitor();
                BitmapPoolUtils.inBitmapThrow(e, options, monitor, configuration.getBitmapPool(), imageUri, 0, 0);
            }

            if (newBitmap != null) {
                builder.append("\n").append("newBitmap: ")
                        .append(Integer.toHexString(newBitmap.hashCode()))
                        .append(", ").append(newBitmap.getWidth()).append("x").append(newBitmap.getHeight())
                        .append(", ").append(newBitmap.isMutable())
                        .append(", ").append(SketchUtils.getBitmapByteSize(newBitmap));
            } else {
                builder.append("\n").append("newBitmap: ").append("null");
            }

            return newBitmap;
        }

        protected void configOptions(BitmapFactory.Options options) {

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            Bitmap oldBitmap = null;
            BitmapDrawable oldDrawable = (BitmapDrawable) imageView.getDrawable();
            if (oldDrawable != null) {
                oldBitmap = oldDrawable.getBitmap();
            }
            imageView.setImageBitmap(bitmap);
            textView.setText(builder.toString());

            if (!BitmapPoolUtils.freeBitmapToPool(oldBitmap, configuration.getBitmapPool())) {
                Log.w("BitmapPoolTest", "recycle");
            }
        }
    }
}
