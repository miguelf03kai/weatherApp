package com.example.miguelf03kai.wetherapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Cards extends RecyclerView.Adapter<Cards.ViewHolder> {

    private static Context context;
    private ArrayList<String> Atime,Atemp,Aspeed,Aimg;

    public Cards(ArrayList<String> Atime,ArrayList<String> Atemp,ArrayList<String> Aspeed,ArrayList<String> Aimg) {
        this.Atime = Atime;
        this.Atemp = Atemp;
        this.Aspeed = Aspeed;
        this.Aimg = Aimg;
    }

    @Override
    public Cards.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list,parent,false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(Cards.ViewHolder holder, int position) {

        holder.temp.setText(Atemp.get(position)+"°c");
        holder.speed.setText(Aspeed.get(position)+ " km/h");

        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");

        try{
            Date t = input.parse(Atime.get(position));
            holder.time.setText(output.format(t));
        }catch(ParseException e){
            e.printStackTrace();
        }

        Picasso.with(Cards.context).load(Aimg.get(position)).into(holder.img);
    }

    @Override
    public int getItemCount() {
        return Atime.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView temp,time,speed;
        public ImageView img;

        public ViewHolder(View itemView){
            super(itemView);
            temp = (TextView) itemView.findViewById(R.id.card);
            time = (TextView) itemView.findViewById(R.id.textView6);
            speed = (TextView) itemView.findViewById(R.id.textView7);
            img = (ImageView) itemView.findViewById(R.id.imageView2);
        }
    }
}