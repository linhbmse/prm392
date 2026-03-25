package com.myfirstandroidjava.salesapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.myfirstandroidjava.salesapp.R;
import com.myfirstandroidjava.salesapp.models.CartItem;
import com.myfirstandroidjava.salesapp.utils.Constants;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems;
    private OnCartActionListener actionListener;

    public interface OnCartActionListener {
        void onUpdateQuantity(CartItem item, int newQuantity);
        void onDeleteItem(CartItem item);
        void onBuyNow(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartActionListener actionListener) {
        this.cartItems = cartItems;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.txtProductName.setText(item.getProductName());
        holder.txtPrice.setText(String.format("$%.2f", item.getPrice()));
        holder.txtQuantity.setText(String.valueOf(item.getQuantity()));
        holder.txtTotal.setText(String.format("Total: $%.2f", item.getTotalPrice()));

        // Load product image with Glide
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

        holder.btnIncrease.setOnClickListener(v -> {
            int max = item.getMaxQuantity();
            if (item.getQuantity() < max) {
                actionListener.onUpdateQuantity(item, item.getQuantity() + 1);
            } else {
                Toast.makeText(v.getContext(), "Số lượng tối đa cho loại này là " + max, Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                actionListener.onUpdateQuantity(item, item.getQuantity() - 1);
            }
        });

        holder.btnDelete.setOnClickListener(v -> actionListener.onDeleteItem(item));
        
        holder.btnBuyNow.setOnClickListener(v -> actionListener.onBuyNow(item));
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public void updateList(List<CartItem> newList) {
        this.cartItems = newList;
        notifyDataSetChanged();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtProductName, txtPrice, txtQuantity, txtTotal;
        ImageButton btnIncrease, btnDecrease, btnDelete;
        Button btnBuyNow;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.cartImage);
            txtProductName = itemView.findViewById(R.id.cartProductName);
            txtPrice = itemView.findViewById(R.id.cartPrice);
            txtQuantity = itemView.findViewById(R.id.cartQuantity);
            txtTotal = itemView.findViewById(R.id.cartTotal);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnBuyNow = itemView.findViewById(R.id.btnBuyNow);
        }
    }
}
