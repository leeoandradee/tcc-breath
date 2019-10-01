package breath.mackenzie.com.br.tcc_breath;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements  BottomNavigationView.OnNavigationItemSelectedListener{

    private ImageView imageView;
    private ProgressBar pgsBar;
    private TextView textView;
    private String serviceResponse;
    private String result;
    private BottomNavigationView bottom_bar;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int GALLERY_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bottom_bar = findViewById(R.id.bottom_navigation);
        bottom_bar.setOnNavigationItemSelectedListener(this);
        bottom_bar.setSelectedItemId(R.id.navigation_home);

        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);
        pgsBar = (ProgressBar)findViewById(R.id.pBar);
        pgsBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#6d89a6"), android.graphics.PorterDuff.Mode.MULTIPLY);

        pgsBar.setVisibility(View.INVISIBLE);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
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
                        IaServiceRequest(imageGallery);
                        showLoadView();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmapCapture = (Bitmap) extras.get("data");
                    String imageCapture = getStringFromBitmap(imageBitmapCapture);
                    imageView.setImageBitmap(imageBitmapCapture);
                    IaServiceRequest(imageCapture);
                    showLoadView();
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
        textView.setVisibility(View.GONE);
        pgsBar.setVisibility(View.VISIBLE);
    }

    private void hideLoadView() {
        pgsBar.setVisibility(View.GONE);
        textView.setVisibility(View.VISIBLE);

    }

    public void IaServiceRequest(final String imagem) {

        RequestQueue queue = Volley.newRequestQueue(this);


        String url = "https://meuherokuapi.herokuapp.com/getLungDisease";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response.toString());
                        result = response.toString();
                        RespostaDto resp = null;
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            RespostaDto res = objectMapper.readValue(result, RespostaDto.class);
                            hideLoadView();

                            DecimalFormat df = new DecimalFormat("#,##0.00 '%'");
                            Double normalPerc = Double.parseDouble(res.getNormal());
                            normalPerc = normalPerc*100;
                            Double atePerc = Double.parseDouble(res.getAtelectasis());
                            atePerc = atePerc*100;
                            Double pneuPerc = Double.parseDouble(res.getPneumonia());
                            pneuPerc = pneuPerc*100;

                            textView.setText("Normal: "+df.format(normalPerc)+"\nAtelectasia: "+df.format(atePerc)+"\nPneumonia: "+df.format(pneuPerc));
                        } catch (IOException e) {
                            e.printStackTrace();
                            hideLoadView();
                            result = "Erro ao processar solicitação";
                            textView.setText(result);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Response", error.toString());
                        hideLoadView();
                        result = "Erro ao processar solicitação";
                        textView.setText(result);
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
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigation_camera:
                dispatchTakePictureIntent();
                bottom_bar.setSelectedItemId(R.id.navigation_home);
                return true;
            case R.id.navigation_gallery:
                pickFromGallery();
                bottom_bar.setSelectedItemId(R.id.navigation_home);
                return true;
        }
        return false;
    }

}
