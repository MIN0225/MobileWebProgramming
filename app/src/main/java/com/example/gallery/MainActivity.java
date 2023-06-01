package com.example.gallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    // on below line we are creating variables for
    // our array list, recycler view and adapter class.
    private static final int PERMISSION_REQUEST_CODE = 200;
    private ArrayList<String> imagePaths;
    private RecyclerView imagesRV;
    private RecyclerViewAdapter imageRVAdapter;
    private ArrayList<Album> albums = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the buttons and the editText
        Button sortButton = findViewById(R.id.sortButton);
        Button searchButton = findViewById(R.id.searchButton);
        EditText searchEditText = findViewById(R.id.searchEditText);

        // creating a new array list and
        // initializing our recycler view.
        imagePaths = new ArrayList<>();
        imagesRV = findViewById(R.id.idRVImages);

        // we are calling a method to request
        // the permissions to read external storage.
        requestPermissions();

        // calling a method to
        // prepare our recycler view.
        prepareRecyclerView();

        // Set a click listener on the button
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Sort images by")
                        .setItems(new CharSequence[]{"Name", "Date"}, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: // Name
                                        sortMediaByName();
                                        break;
                                    case 1: // Date
                                        sortMediaByDate();
                                        break;
                                }
                                // Show a toast message
                                Toast.makeText(MainActivity.this, "Sorting images...", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTerm = searchEditText.getText().toString();
                searchMedia(searchTerm);
            }
        });

        // creating a new array list and
        // initializing our recycler view.
        imagePaths = new ArrayList<>();
        imagesRV = findViewById(R.id.idRVImages);

        // we are calling a method to request
        // the permissions to read external storage.
        requestPermissions();

        // calling a method to
        // prepare our recycler view.
        prepareRecyclerView();

    }

    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (checkPermission()) {
            Toast.makeText(this, "Permissions granted..", Toast.LENGTH_SHORT).show();
            getImagePath();
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void prepareRecyclerView() {

        // in this method we are preparing our recycler view.
        // on below line we are initializing our adapter class.
        imageRVAdapter = new RecyclerViewAdapter(MainActivity.this, imagePaths);

        // on below line we are creating a new grid layout manager.
        GridLayoutManager manager = new GridLayoutManager(MainActivity.this, 4);

        // on below line we are setting layout
        // manager and adapter to our recycler view.
        imagesRV.setLayoutManager(manager);
        imagesRV.setAdapter(imageRVAdapter);
    }

    private void getImagePath() {
        // in this method we are adding all our image paths
        // in our arraylist which we have created.
        // 기기에 외부 SD 카드가 마운트되어 있는지 확인
        boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        final String orderBy = MediaStore.Images.Media._ID;

        // For external SD card
        if (isSDPresent) {
            Cursor externalCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
            addImagesFromCursorToArrayList(externalCursor);
        }

        // For internal storage
        Cursor internalCursor = getContentResolver().query(MediaStore.Images.Media.INTERNAL_CONTENT_URI, columns, null, null, orderBy);
        addImagesFromCursorToArrayList(internalCursor);
    }

    private void addImagesFromCursorToArrayList(Cursor cursor) {
        if (cursor != null) {
            int count = cursor.getCount();
            for (int i = 0; i < count; i++) {
                cursor.moveToPosition(i);
                int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                imagePaths.add(cursor.getString(dataColumnIndex));
            }
            if (imageRVAdapter != null) {
                imageRVAdapter.notifyDataSetChanged();
            } else {
                prepareRecyclerView();
            }
            cursor.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // this method is called after permissions has been granted.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // we are checking the permission code.
            case PERMISSION_REQUEST_CODE:
                // in this case we are checking if the permissions are accepted or not.
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        // if the permissions are accepted we are displaying a toast message
                        // and calling a method to get image path.
                        Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show();
                        getImagePath();
                    } else {
                        // if permissions are denied we are closing the app and displaying the toast message.
                        Toast.makeText(this, "권한 요청 거부 Permissions are required to use the app..", Toast.LENGTH_SHORT).show();
                        requestPermissions(); // request for permissions again
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show();
                    getImagePath();
                } else {
                    Toast.makeText(this, "권한 요청 거부 Permissions are required to use the app..", Toast.LENGTH_SHORT).show();
                    requestPermissions(); // request for permissions again
                }
            }
        }
    }

    public void sortMediaByName() {
        Collections.sort(imagePaths);
        imageRVAdapter.notifyDataSetChanged();
    }

    public void sortMediaByDate() {
        try {
            Collections.sort(imagePaths, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    File file1 = new File(o1);
                    File file2 = new File(o2);
                    if (!file1.exists() || !file1.canRead() || !file2.exists() || !file2.canRead()) {
                        Toast.makeText(MainActivity.this, "File doesn't exist or can't be read", Toast.LENGTH_SHORT).show();
                        return 0;
                    }
                    long lastModified1 = file1.lastModified();
                    long lastModified2 = file2.lastModified();
                    return Long.compare(lastModified1, lastModified2);
                }
            });
            imageRVAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Error sorting by date: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void sortMedia() {
        // Create a dialog to ask the user how they want to sort the media
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort media by:");
        String[] sortOptions = {"Name", "Date"};
        builder.setItems(sortOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Name
                        sortMediaByName();
                        break;
                    case 1: // Date
                        sortMediaByDate();
                        break;
                }
            }
        });
        builder.show();
    }

    public void searchMedia(String searchTerm) {
        ArrayList<String> searchResults = new ArrayList<>();
        for (String path : imagePaths) {
            File file = new File(path);
            if (file.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                searchResults.add(path);
            }
        }
        imageRVAdapter = new RecyclerViewAdapter(MainActivity.this, searchResults);
        imagesRV.setAdapter(imageRVAdapter);
    }

    public void createAlbum(String name) {
        Album album = new Album(name);
        albums.add(album);
    }

    public void renameAlbum(String oldName, String newName) {
        for (Album album : albums) {
            if (album.getName().equals(oldName)) {
                album.setName(newName);
                break;
            }
        }
    }

    public void deleteAlbum(String name) {
        for (Album album : albums) {
            if (album.getName().equals(name)) {
                albums.remove(album);
                break;
            }
        }
    }

    public void addImageToAlbum(String albumName, String imagePath) {
        for (Album album : albums) {
            if (album.getName().equals(albumName)) {
                album.addImage(imagePath);
                break;
            }
        }
    }

    public void removeImageFromAlbum(String albumName, String imagePath) {
        for (Album album : albums) {
            if (album.getName().equals(albumName)) {
                album.removeImage(imagePath);
                break;
            }
        }
    }
}