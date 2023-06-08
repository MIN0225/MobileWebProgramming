package com.example.gallery;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.io.File;

public class ImageDetailActivity extends AppCompatActivity {

    // 문자열 변수, 이미지 뷰 변수, 스케일 제스처 검출기 클래스 변수를 생성합니다.
    String imgPath;
    private ImageView imageView;
    private ScaleGestureDetector scaleGestureDetector;

    // 아래 줄에서 스케일 요소를 정의합니다.
    private float mScaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        // 아래 줄에서 어댑터 클래스에서 전달한 데이터를 얻습니다.
        imgPath = getIntent().getStringExtra("imgPath");

        // 이미지 뷰를 초기화합니다.
        imageView = findViewById(R.id.idIVImage);

        // 아래 줄에서 우리의 이미지를 확대 축소하기 위한 스케일 제스처 검출기를 초기화합니다.
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        // 아래 줄에서 이미지 파일을 경로에서 가져옵니다.
        File imgFile = new File(imgPath);

        // 파일이 존재하면 해당 이미지를 이미지 뷰에 로드합니다.
        if (imgFile.exists()) {
            Picasso.get().load(imgFile).placeholder(R.drawable.ic_launcher_background).into(imageView);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // 터치 이벤트 메서드 안에서, 터치 이벤트 메서드를 호출하고 우리의 모션 이벤트를 전달합니다.
        scaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        // 아래 줄에서 스케일 리스너를 위한 클래스를 생성하고 제스처 리스너로 확장합니다.
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

            // 스케일 메서드 내에서, 이미지 뷰의 이미지에 대한 스케일을 설정합니다.
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            // 아래 줄에서 이미지 뷰의 scale x와 scale y를 설정합니다.
            imageView.setScaleX(mScaleFactor);
            imageView.setScaleY(mScaleFactor);
            return true;
        }
    }
}
