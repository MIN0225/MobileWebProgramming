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

    private static final int PERMISSION_REQUEST_CODE = 200;

    // 원래의 이미지 경로를 저장하는 ArrayList
    private ArrayList<String> originalImagePaths;
    // 검색 결과를 저장하는 ArrayList
    private ArrayList<String> searchResults;

    private RecyclerView imagesRV;
    private RecyclerViewAdapter imageRVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Button, EditText 초기화
        Button sortButton = findViewById(R.id.sortButton);
        Button searchButton = findViewById(R.id.searchButton);
        EditText searchEditText = findViewById(R.id.searchEditText);

        // 이미지 경로를 저장할 ArrayList 생성
        originalImagePaths = new ArrayList<>();
        searchResults = new ArrayList<>();

        // 리사이클러뷰 초기화
        imagesRV = findViewById(R.id.idRVImages);

        // 권한 요청
        requestPermissions();

        // 리사이클러뷰 준비 메서드 호출
        prepareRecyclerView(originalImagePaths);

        // 정렬 버튼에 클릭 리스너를 설정합니다.
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AlertDialog를 생성합니다.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Sort images by")
                        .setItems(new CharSequence[]{"Name", "Date"}, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: // Name
                                        sortMediaByName(); // 이름 순으로 정렬하는 메서드를 호출합니다.
                                        break;
                                    case 1: // Date
                                        sortMediaByDate(); // 날짜 순으로 정렬하는 메서드를 호출합니다.
                                        break;
                                }
                                // 정렬이 시작됨을 사용자에게 알리는 토스트 메시지를 보여줍니다.
                                Toast.makeText(MainActivity.this, "Sorting images...", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });

        // 검색 버튼에 클릭 리스너를 설정합니다.
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTerm = searchEditText.getText().toString();
                searchMedia(searchTerm);
            }
        });

        // 같은 이미지 중복으로 나타내는 걸 방지하기 위함. getImagePath()메서드가 호출될때마다 originalImagePaths에 이미지 경로가 계속 추가된다.
        // RecyclerView 초기화
        originalImagePaths = new ArrayList<>();
        imagesRV = findViewById(R.id.idRVImages);

        // 외부 저장소 읽기 권한 요청
        requestPermissions();

        // RecyclerView 준비 메서드 호출
        prepareRecyclerView(originalImagePaths);
    }

    /**
     * 저장소에 읽기 권한이 있는지 확인
     * @return 권한이 있으면 true, 없으면 false
     */
    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) { // 안드로이드 버전이 R(11) 이상인지 확인
            return Environment.isExternalStorageManager(); // 앱이 '모든 파일에 대한 접근 권한'을 가지고 있으면 true를 반환하고, 그렇지 않으면 false를 반환
        } else { // 만약 Android 버전이 R 이전이면
            int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE); // 권한을 가지고 있는지 확인
            return result == PackageManager.PERMISSION_GRANTED; // 앱이 이 권한을 가지고 있으면 true를 반환하고, 그렇지 않으면 false를 반환
        }
    }

    /**
     * 앱이 사용자의 파일 시스템에 접근할 수 있는지 확인하고, 필요한 경우 사용자에게 필요한 권한을 부여하도록 요청
     */
    private void requestPermissions() {
        if (checkPermission()) { // 앱이 이미 필요한 권한을 가지고 있는지 확인
            Toast.makeText(this, "Permissions granted..", Toast.LENGTH_SHORT).show(); //만약 권한이 있으면, "Permissions granted.." 메시지를 토스트로 보여준다.
            getImagePath(); // 이미지 경로를 가져온다
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) { // Android R(11) 이상인지 버전확인
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION); // 시스템 설정 화면으로 이동하여 사용자가 앱에 파일 시스템 접근을 허용하도록 합니다.
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            } else { // Android R 이전 버전
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE); //  시스템에 권한 요청 다이얼로그를 표시하고, 사용자가 권한을 허용하거나 거부할 수 있게 합니다.
            }
        }
    }

    /**
     * RecyclerView를 초기화하고 설정하는 역할
     */
    private void prepareRecyclerView(ArrayList<String> imagePaths) {

        // RecyclerView에 사용될 adapter를 초기화
        imageRVAdapter = new RecyclerViewAdapter(MainActivity.this, imagePaths);

        // GridLayoutManager를 초기화
        // RecyclerView의 아이템을 격자형태로 배치하는 레이아웃 매니저
        // 두 번째 파라미터로 전달된 4는 한 행에 4개의 아이템을 배치하도록 설정
        GridLayoutManager manager = new GridLayoutManager(MainActivity.this, 4);

        // RecyclerView에 이 GridLayoutManager를 설정
        // 이를 통해 RecyclerView는 격자형태로 아이템을 배치
        imagesRV.setLayoutManager(manager);

        // RecyclerView에 adapter를 설정
        // 이를 통해 RecyclerView는 각 아이템에 해당하는 이미지를 알 수 있게 됩니다.
        imagesRV.setAdapter(imageRVAdapter);
    }

    /**
     * 이미지 파일 경로를 가져오는 메서드
     */
    private void getImagePath() {
        // 기기에 외부 SD 카드가 마운트되어 있는지 확인
        boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        final String orderBy = MediaStore.Images.Media._ID;

        // 외부 SD 카드가 있다면 외부 SD 카드에서 이미지를 가져옴
        if (isSDPresent) {
            Cursor externalCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
            addImagesFromCursorToArrayList(externalCursor);
        }

        // 내부 저장소에서 이미지를 가져옴
        Cursor internalCursor = getContentResolver().query(MediaStore.Images.Media.INTERNAL_CONTENT_URI, columns, null, null, orderBy);
        addImagesFromCursorToArrayList(internalCursor);
    }

    /**
     * 커서에서 이미지를 가져와 ArrayList에 추가하는 메서드
     */
    private void addImagesFromCursorToArrayList(Cursor cursor) {
        if (cursor != null) { // null이면 이미지 파일이 없다는 뜻
            int count = cursor.getCount(); // Cursor 객체가 참조하는 행의 수를 가져옵니다. 이 경우, 각 행은 이미지 파일 하나를 나타냅니다.
            // 커서의 각 위치에서 이미지를 가져와 ArrayList에 추가
            for (int i = 0; i < count; i++) {
                cursor.moveToPosition(i); // Cursor를 i번째 행으로 이동시킵니다.
                int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);  // Cursor가 참조하는 행에서 MediaStore.Images.Media.DATA 열의 인덱스를 가져옵니다. 이 열에는 이미지 파일의 경로가 저장되어 있습니다.
                originalImagePaths.add(cursor.getString(dataColumnIndex)); // Cursor가 참조하는 현재 행에서 MediaStore.Images.Media.DATA 열의 값을 가져와 originalImagePaths에 추가합니다.
            }
            // 이미지가 추가된 후에는 RecyclerView의 adapter를 업데이트하거나, RecyclerView를 준비
            if (imageRVAdapter != null) {
                imageRVAdapter.notifyDataSetChanged();
            } else {
                prepareRecyclerView(originalImagePaths); // RecyclerView를 초기화하고 설정
            }
            cursor.close(); // Cursor를 닫습니다. 이는 Cursor가 차지하는 자원을 해제하는 데 필요합니다. Cursor를 더 이상 사용하지 않을 때는 항상 닫아야 합니다.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // 권한이 승인된 후 호출되는 메서드
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // we are checking the permission code.
            case PERMISSION_REQUEST_CODE:
                // 권한이 승인되었는지 확인
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        // 권한이 승인되었다면 토스트 메시지를 표시하고 이미지 경로를 가져옴
                        Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show();
                        getImagePath();
                    } else {
                        // 권한이 거부되었다면 토스트 메시지를 표시하고 다시 권한을 요청
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
                    // 권한이 승인되었다면 토스트 메시지를 표시하고 이미지 경로를 가져옴
                    Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show();
                    getImagePath();
                } else {
                    // 권한이 거부되었다면 토스트 메시지를 표시하고 다시 권한을 요청
                    Toast.makeText(this, "권한 요청 거부 Permissions are required to use the app..", Toast.LENGTH_SHORT).show();
                    requestPermissions(); // request for permissions again
                }
            }
        }
    }

    /**
     * 이미지를 이름순으로 정렬하는 메서드
     */
    public void sortMediaByName() {
        if (searchResults.isEmpty()) {
            Collections.sort(originalImagePaths);
        } else {
            Collections.sort(searchResults);
        }
        imageRVAdapter.notifyDataSetChanged(); // RecyclerView에게 데이터가 변경되었음을 알려줌
    }

    /**
     * 이미지를 날짜순으로 정렬하는 메서드
     */
    public void sortMediaByDate() {
        try {
            if(searchResults.isEmpty()){
                Collections.sort(originalImagePaths, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        File file1 = new File(o1);
                        File file2 = new File(o2);
                        // 파일이 존재하지 않거나 읽을 수 없는 경우, 토스트 메시지를 표시
                        if (!file1.exists() || !file1.canRead() || !file2.exists() || !file2.canRead()) {
                            Toast.makeText(MainActivity.this, "File doesn't exist or can't be read", Toast.LENGTH_SHORT).show();
                            return 0;
                        }
                        // 파일의 마지막 수정 날짜를 기준으로 비교
                        long lastModified1 = file1.lastModified();
                        long lastModified2 = file2.lastModified();
                        return Long.compare(lastModified1, lastModified2);
                    }
                });
            } else{
                Collections.sort(searchResults, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        File file1 = new File(o1);
                        File file2 = new File(o2);
                        // 파일이 존재하지 않거나 읽을 수 없는 경우, 토스트 메시지를 표시
                        if (!file1.exists() || !file1.canRead() || !file2.exists() || !file2.canRead()) {
                            Toast.makeText(MainActivity.this, "File doesn't exist or can't be read", Toast.LENGTH_SHORT).show();
                            return 0;
                        }
                        // 파일의 마지막 수정 날짜를 기준으로 비교
                        long lastModified1 = file1.lastModified();
                        long lastModified2 = file2.lastModified();
                        return Long.compare(lastModified1, lastModified2);
                    }
                });
            }
            imageRVAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            // 오류가 발생하면 토스트 메시지로 오류 메시지를 표시
            Toast.makeText(MainActivity.this, "Error sorting by date: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



    /**
     * 검색어에 일치하는 미디어를 검색하는 메서드
     */
    public void searchMedia(String searchTerm) {
        searchResults.clear(); // 검색 결과를 초기화합니다.
        // 모든 이미지 경로를 순회하면서 파일 이름이 검색어를 포함하는지 확인
        for (String path : originalImagePaths) {
            File file = new File(path);
            if (file.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                // 검색어를 포함하는 파일이면 결과 리스트에 추가
                searchResults.add(path);
            }
        }
        prepareRecyclerView(searchResults); // 검색 결과를 표시합니다.
    }
}