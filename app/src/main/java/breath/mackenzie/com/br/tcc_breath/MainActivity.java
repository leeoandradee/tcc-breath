package breath.mackenzie.com.br.tcc_breath;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ImageButton takePictureButton;
    private ImageButton choosePictureButton;
    private ImageView imageView;
    private ProgressBar pgsBar;
    private TextView textView;
    private String serviceResponse;
    private String result;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int GALLERY_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePictureButton = (ImageButton) findViewById(R.id.takePictureButton);
        choosePictureButton = (ImageButton) findViewById(R.id.choosePictureButton);
        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);
        pgsBar = (ProgressBar)findViewById(R.id.pBar);
        pgsBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#6d89a6"), android.graphics.PorterDuff.Mode.MULTIPLY);

        pgsBar.setVisibility(View.INVISIBLE);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            takePictureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showLoadView();
                dispatchTakePictureIntent();
            }
        });

        choosePictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showLoadView();
                pickFromGallery();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takePictureButton.setEnabled(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        String imageGallery = getStringFromBitmap(bitmap);
                        imageView.setImageBitmap(bitmap);
                        textView.setText(IaServiceRequest(imageGallery));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmapCapture = (Bitmap) extras.get("data");
                    String imageCapture = getStringFromBitmap(imageBitmapCapture);
                    imageView.setImageBitmap(imageBitmapCapture);
                    textView.setText(IaServiceRequest(imageCapture));
                    break;
            }

        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void pickFromGallery(){
        Intent choosePictureIntent = new Intent(Intent.ACTION_PICK);
        choosePictureIntent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        choosePictureIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(choosePictureIntent, GALLERY_REQUEST_CODE);
    }

    private void showLoadView() {
        imageView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.INVISIBLE);
        pgsBar.setVisibility(View.VISIBLE);
        textView.setText("carregando...");
    }

    public String IaServiceRequest(final String imagem) {

        RequestQueue queue = Volley.newRequestQueue(this);


        String url = "http://10.0.2.2:5002/getLungDisease";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        result = response.toString();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("imagem", imagem);

                return params;
            }
        };
        queue.add(postRequest);
        return result;
    }

    private String getStringFromBitmap(Bitmap bitmapPicture) {
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    public Bitmap loadBitmap(String url)
    {
        Bitmap bm = null;
        InputStream is = null;
        BufferedInputStream bis = null;
        try
        {
            URLConnection conn = new URL(url).openConnection();
            conn.connect();
            is = conn.getInputStream();
            bis = new BufferedInputStream(is, 8192);
            bm = BitmapFactory.decodeStream(bis);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if (bis != null)
            {
                try
                {
                    bis.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return bm;
    }


}
