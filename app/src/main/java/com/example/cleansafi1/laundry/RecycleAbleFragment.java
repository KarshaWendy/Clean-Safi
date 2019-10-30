package com.example.cleansafi1.laundry;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleansafi1.R;
import com.example.cleansafi1.utils.AppGlobals;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.example.cleansafi1.laundry.LaundryCategoriesActivity.order;
import static com.example.cleansafi1.laundry.LaundryCategoriesActivity.wholeData;



@SuppressLint("ValidFragment")
public class RecycleAbleFragment extends Fragment {

    public RecyclerView mRecyclerView;
    private static RecycleAbleFragment sInstance;
    private View mBaseView;
    private GridLayoutManager gridLayoutManager;
    private String fragmentName;

    public static RecycleAbleFragment getInstance() {
        return sInstance;
    }

    public RecycleAbleFragment(String fragment) {
        fragmentName = fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.recyclerable_layout, container, false);
        return mBaseView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sInstance = this;
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        gridLayoutManager = new GridLayoutManager(getActivity()
                .getApplicationContext(), 2);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.specific_recycler);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.canScrollVertically(LinearLayoutManager.VERTICAL);
        mRecyclerView.setHasFixedSize(true);
        CustomAdapter mAdapter = new CustomAdapter(wholeData.get(fragmentName));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new CustomAdapter(wholeData.get(fragmentName),
                getActivity().getApplicationContext(),
                new OnItemClickListener() {
                    @Override
                    public void onItem(Integer item) {

                    }
                }));
    }

    private class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
            RecyclerView.OnItemTouchListener {

        private ArrayList<LaundryItem> items;
        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;
        private CustomView viewHolder;

        public CustomAdapter(ArrayList<LaundryItem> categories, Context context, OnItemClickListener listener) {
            this.items = categories;
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        public CustomAdapter(ArrayList<LaundryItem> categories) {
            this.items = categories;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.delegate_category, parent, false);
            viewHolder = new CustomView(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            holder.setIsRecyclable(false);
            final LaundryItem laundryItem = items.get(position);
            String titleLowerCase = laundryItem.getName();
            String firstUpper = titleLowerCase.substring(0, 1).toUpperCase() + titleLowerCase.substring(1);
            viewHolder.titleTextView.setText(firstUpper);
            viewHolder.price.setText(String.valueOf(laundryItem.getPrice() + " SAR"));
            Picasso.with(AppGlobals.getContext())
                    .load(laundryItem.getImageUri())
                    .resize(300, 300)
                    .centerCrop()
                    .into(viewHolder.imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (mRecyclerView.findViewHolderForAdapterPosition(position) != null) {

                            }
                        }

                        @Override
                        public void onError() {
                            if (mRecyclerView.findViewHolderForAdapterPosition(position) != null) {

                            }

                        }
                    });
            if (order.containsKey(items.get(position).getId())) {
                if (mRecyclerView.findViewHolderForAdapterPosition(position) != null) {
                    RelativeLayout relativeLayout = (RelativeLayout) mRecyclerView
                            .findViewHolderForAdapterPosition(holder.getPosition()).
                                    itemView.findViewById(R.id.layout);
                    relativeLayout.setBackgroundColor(getResources()
                            .getColor(R.color.card_selected_color));
                }
            }  else {
                if (mRecyclerView.findViewHolderForAdapterPosition(position) != null) {
                    RelativeLayout relativeLayout = (RelativeLayout) mRecyclerView
                            .findViewHolderForAdapterPosition(holder.getPosition()).
                                    itemView.findViewById(R.id.layout);
                    relativeLayout.setBackgroundColor(getResources()
                            .getColor(android.R.color.white));
                }
            }
            viewHolder.plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView quantity = (TextView) mRecyclerView
                            .findViewHolderForAdapterPosition(holder.getPosition())
                            .itemView.findViewById(R.id.quantity_text_view);
                    String qty = quantity.getText().toString();
                    if (Integer.valueOf(qty) == 10) {
                        Toast.makeText(getActivity(), "limit reached", Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                        return;
                    }
                    if (order.containsKey(items.get(position).getId())) {
                        OrderItem orderItem = order.get(items.get(position).getId());
//                        Log.i("TAG", "order item" + String.valueOf(orderItem.getQuantity()));
                        orderItem.setQuantity(String.valueOf(Integer.valueOf(qty) + 1));
                        order.put(orderItem.getId(), orderItem);
                        Log.i("TAG", "added " + orderItem.getQuantity());
//                        Log.i("TAG", "item" + String.valueOf(orderItem.getQuantity()));
                        TextView textView = (TextView) mRecyclerView
                                .findViewHolderForAdapterPosition(holder.getPosition())
                                .itemView.findViewById(R.id.quantity_text_view);
                        textView.setText(orderItem.getQuantity());
                    } else {
                        Log.i("TAG", "else part");
                        OrderItem orderItem = new OrderItem();
                        orderItem.setId(items.get(position).getId());
                        orderItem.setName(items.get(position).getName());
                        orderItem.setQuantity(String.valueOf(1));
                        orderItem.setPrice(items.get(position).getPrice());
                        orderItem.setImageUrl(items.get(position).getImageUri());
                        order.put(items.get(position).getId(),
                                orderItem);
                        Log.i("TAG", "get quantity" + orderItem.getQuantity());
                        RelativeLayout relativeLayout = (RelativeLayout) mRecyclerView
                                .findViewHolderForAdapterPosition(holder.getPosition()).
                                itemView.findViewById(R.id.layout);
                        relativeLayout.setBackgroundColor(getResources()
                                .getColor(R.color.card_selected_color));
                        TextView textView = (TextView) mRecyclerView.findViewHolderForAdapterPosition(
                                holder.getPosition()
                        ).itemView.findViewById(R.id.quantity_text_view);
                        textView.setText(String.valueOf(orderItem.getQuantity()));
                    }
                }
            });
            viewHolder.minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView quantity = (TextView) mRecyclerView
                            .findViewHolderForAdapterPosition(holder.getPosition())
                            .itemView.findViewById(R.id.quantity_text_view);
                    String qty = quantity.getText().toString();
                    Log.i("TAG", "qty " + qty);
                    Log.i("TAG", "position " + position);
                    OrderItem orderItem = order.get(items.get(position).getId());
                    if (Integer.valueOf(qty) == 1 || Integer.valueOf(qty) < 1) {
                        order.remove(orderItem.getId());
                        Toast.makeText(getActivity(), "Item removed", Toast.LENGTH_SHORT).show();
                        RelativeLayout relativeLayout = (RelativeLayout) mRecyclerView
                                .findViewHolderForAdapterPosition(holder.getPosition()).
                                        itemView.findViewById(R.id.layout);
                        relativeLayout.setBackgroundColor(getResources()
                                .getColor(android.R.color.white));
                        TextView textView = (TextView) mRecyclerView.findViewHolderForAdapterPosition(
                                holder.getPosition()
                        ).itemView.findViewById(R.id.quantity_text_view);
                        textView.setText(String.valueOf(0));
                        return;
                    }
                    if (Integer.valueOf(qty) == 0) {
                        return;
                    }
                    orderItem.setQuantity(String.valueOf(Integer.valueOf(qty) - 1));
                    order.put(orderItem.getId(), orderItem);
                    TextView textView = (TextView) mRecyclerView.findViewHolderForAdapterPosition(
                            holder.getPosition()
                    ).itemView.findViewById(R.id.quantity_text_view);
                    textView.setText(String.valueOf(orderItem.getQuantity()));
                    Log.i("TAG", "minusButton click");
                }
            });
//            new android.os.Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (order.containsKey(laundryItem.getId())) {
//                        RelativeLayout relativeLayout = (RelativeLayout) mRecyclerView.findViewHolderForAdapterPosition(position).
//                                itemView.findViewById(R.id.layout);
//                        relativeLayout.setBackgroundColor(getResources()
//                                .getColor(R.color.card_selected_color));
//                    } else {
//                        RelativeLayout relativeLayout = (RelativeLayout) mRecyclerView.findViewHolderForAdapterPosition(position).
//                                itemView.findViewById(R.id.layout);
//                        relativeLayout.setBackgroundColor(getResources()
//                                .getColor(android.R.color.white));
//                    }
//                }
//            }, 10);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View childView = rv.findChildViewUnder(e.getX(), e.getY());
//            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e) &&
//                    (!Spinner.class.isInstance(childView)) && (!ImageButton.class.isInstance(childView))) {
//                mListener.onItem(items.get(rv.getChildPosition(childView)).getId());
//                setBackgroundColor(rv, childView);
//                return true;
//            }
            return false;
        }
//
//        private void setBackgroundColor(RecyclerView.ViewHolder rv, int position) {
//            if (order.containsKey(items.get(position).getId())) {
//                TextView quantityTextView = (TextView) mRecyclerView
//                        .findViewHolderForAdapterPosition(rv.getChildPosition(childView)).
//                                itemView.findViewById(R.id.quantity_text_view);
//                if (Integer.valueOf(quantityTextView.getText().toString()) == 10) {
//                    Toast.makeText(getActivity(), "limit reached", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                order.remove(items.get(rv.getChildPosition(childView)).getId());
//                OrderItem orderItem = new OrderItem();
//                orderItem.setId(items.get(rv.getChildPosition(childView)).getId());
//                orderItem.setName(items.get(rv.getChildPosition(childView)).getName());
//                orderItem.setQuantity(String.valueOf(
//                        Integer.valueOf(quantityTextView.getText().toString())+1));
//                orderItem.setPrice(items.get(rv.getChildPosition(childView)).getPrice());
//                orderItem.setImageUrl(items.get(rv.getChildPosition(childView)).getImageUri());
//                order.put(items.get(rv.getChildPosition(childView)).getId(),
//                        orderItem);
//                quantityTextView.setText(String.valueOf(orderItem.getQuantity()));
//                quantityTextView.setTextColor(Color.WHITE);
//            } else {
//                RelativeLayout cardView = (RelativeLayout) mRecyclerView
//                        .findViewHolderForAdapterPosition(rv.getChildPosition(childView)).
//                                itemView.findViewById(R.id.layout);
//                cardView.setBackgroundColor(getResources()
//                        .getColor(R.color.card_selected_color));
//                OrderItem orderItem = new OrderItem();
//                orderItem.setId(items.get(rv.getChildPosition(childView)).getId());
//                orderItem.setName(items.get(rv.getChildPosition(childView)).getName());
//                orderItem.setQuantity(String.valueOf(1));
//                orderItem.setPrice(items.get(rv.getChildPosition(childView)).getPrice());
//                orderItem.setImageUrl(items.get(rv.getChildPosition(childView)).getImageUri());
//                order.put(items.get(rv.getChildPosition(childView)).getId(),
//                        orderItem);
//                TextView quantityTextView = (TextView) mRecyclerView
//                        .findViewHolderForAdapterPosition(rv.getChildPosition(childView)).
//                                itemView.findViewById(R.id.quantity_text_view);
//                quantityTextView.setText(String.valueOf(orderItem.getQuantity()));
//                quantityTextView.setTextColor(Color.WHITE);
//            }
//        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public interface OnItemClickListener {
        void onItem(Integer item);
    }

    private static class CustomView extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public ImageView imageView;
        public TextView price;
        public RelativeLayout relativeLayout;
        public CardView cardView;
        public TextView quantityTextView;
        public ImageButton plus;
        public ImageButton minus;

        public CustomView(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.name);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            price = (TextView) itemView.findViewById(R.id.price);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.layout);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            quantityTextView = (TextView) itemView.findViewById(R.id.quantity_text_view);
            plus = (ImageButton) itemView.findViewById(R.id.plus);
            minus = (ImageButton) itemView.findViewById(R.id.minus);
            quantityTextView.setTypeface(AppGlobals.typefaceNormal);
            titleTextView.setTypeface(AppGlobals.typefaceNormal);
            price.setTypeface(AppGlobals.typefaceNormal);
            titleTextView.setTypeface(AppGlobals.typefaceNormal);
        }
    }
}
