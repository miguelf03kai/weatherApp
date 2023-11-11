package com.miguelf03kai.weatherapp.cards;;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.miguelf03kai.weatherapp.R;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CardView extends RecyclerView.Adapter<CardView.ViewHolder> {

    private static Context context;
    private ArrayList<String> arrayTime, arrayTemp, arraySpeed, arrayImage;

    public CardView(ArrayList<String> arrayTime,ArrayList<String> arrayTemp,ArrayList<String> arraySpeed,
                    ArrayList<String> arrayImage) {
        this.arrayTime = arrayTime;
        this.arrayTemp = arrayTemp;
        this.arraySpeed = arraySpeed;
        this.arrayImage = arrayImage;
    }

    @Override
    public CardView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_list,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CardView.ViewHolder holder, int position) {

        holder.temp.setText(arrayTemp.get(position)+"°c");
        holder.speed.setText(arraySpeed.get(position)+ " km/h");

        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");

        try{
            Date t = input.parse(arrayTime.get(position));
            holder.time.setText(output.format(t));
        }catch(ParseException e){
            e.printStackTrace();
        }

        Picasso.with(CardView.context).load(arrayImage.get(position)).into(holder.conditionIconCardImageView);
    }

    @Override
    public int getItemCount() {
        return arrayTime.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView temp, time, speed;
        public ImageView conditionIconCardImageView;

        public ViewHolder(View itemView){
            super(itemView);
            temp = (TextView) itemView.findViewById(R.id.tempTextView);
            time = (TextView) itemView.findViewById(R.id.timeTextView);
            speed = (TextView) itemView.findViewById(R.id.speedTextView);
            conditionIconCardImageView = (ImageView) itemView.findViewById(R.id.conditionIconCardImageView);
        }
    }
}