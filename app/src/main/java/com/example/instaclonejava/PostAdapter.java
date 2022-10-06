package com.example.instaclonejava;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instaclonejava.databinding.RecyclerowBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostHolder> {
    ArrayList<Post> postList;

    public PostAdapter(ArrayList<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerowBinding recyclerowBinding = RecyclerowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return  new PostHolder(recyclerowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        holder.binding.email.setText(postList.get(position).email);
        holder.binding.comment.setText(postList.get(position).comment);
        Picasso.get().load(postList.get(position).dowloadedUrl).into(holder.binding.image);

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostHolder extends RecyclerView.ViewHolder{
        private RecyclerowBinding
        public PostHolder(@NonNull RecyclerowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
