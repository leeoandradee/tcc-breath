package breath.mackenzie.com.br.tcc_breath;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import java.io.File;
import java.io.IOException;
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
                    imageView.setImageURI(selectedImage);
                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imageView.setImageBitmap(imageBitmap);
                    //IaServiceRequest(imageBitmap.toString());
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
        String url ="http://www.google.com";


        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        serviceResponse = response;
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        serviceResponse = error.toString();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("imagem", imagem);
                return params;
            }
        };
        queue.add(postRequest);
        return serviceResponse;
    }

}
