package com.myfirstandroidjava.salesapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.myfirstandroidjava.salesapp.R;
import com.myfirstandroidjava.salesapp.models.OrderItem;
import com.myfirstandroidjava.salesapp.utils.Constants;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<OrderItem> orderItems;

    public OrderAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);
        holder.productName.setText(item.getProductName());
        holder.quantity.setText("Qty: " + item.getQuantity());
        holder.price.setText(String.format("$%.2f", item.getPrice()));

        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (!imageUrl.startsWith("http")) {
                imageUrl = Constants.BASE_URL + imageUrl;
            }
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.image_error)
                    .into(holder.imgProduct);
        }
    }

    @Override
    public int getItemCount() {
        return orderItems != null ? orderItems.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView productName, quantity, price;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.cartImage);
            productName = itemView.findViewById(R.id.cartProductName);
            quantity = itemView.findViewById(R.id.cartQuantity);
            price = itemView.findViewById(R.id.cartPrice);
        }
    }
}
