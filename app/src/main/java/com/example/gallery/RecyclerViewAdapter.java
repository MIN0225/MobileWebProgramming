package com.example.gallery;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

    // 컨텍스트와 이미지 경로 배열을 위한 변수를 생성합니다.
    private final Context context;
    private final ArrayList<String> imagePathArrayList;

    // 아래 줄에서 생성자를 생성했습니다.
    public RecyclerViewAdapter(Context context, ArrayList<String> imagePathArrayList) {
        this.context = context;
        this.imagePathArrayList = imagePathArrayList;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 이 메서드에서 우리가 생성한 레이아웃을 인플레이트합니다.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new RecyclerViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        int pos = holder.getAdapterPosition();
        if (pos != RecyclerView.NO_POSITION) {
            File imgFile = new File(imagePathArrayList.get(pos));
            if (imgFile.exists()) {
                // 이미지 파일이 존재하면, 그 이미지를 뷰 홀더의 이미지 뷰에 로드합니다.
                Picasso.get().load(imgFile).placeholder(R.drawable.ic_launcher_background).into(holder.imageIV);
                // 각 이미지 아이템에 클릭 리스너를 설정합니다. 클릭하면 해당 이미지의 상세 페이지로 이동합니다.
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int clickPosition = holder.getAdapterPosition();
                        if (clickPosition != RecyclerView.NO_POSITION) {
                            Intent i = new Intent(context, ImageDetailActivity.class);
                            i.putExtra("imgPath", imagePathArrayList.get(clickPosition));
                            context.startActivity(i);
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        // 이 메서드는 리사이클러뷰의 크기를 반환합니다.
        return imagePathArrayList.size();
    }

    // View Holder 클래스는 리사이클러뷰를 다루기 위해 만들어졌습니다.
    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {

        // 뷰들에 대한 변수를 생성합니다.
        private final ImageView imageIV;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            // 뷰들을 그들의 id를 통해 초기화합니다.
            imageIV = itemView.findViewById(R.id.idIVImage);
        }
    }
}
