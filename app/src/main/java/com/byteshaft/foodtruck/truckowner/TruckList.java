package com.byteshaft.foodtruck.truckowner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.byteshaft.foodtruck.R;
import com.byteshaft.foodtruck.accounts.LoginActivity;
import com.byteshaft.foodtruck.utils.AppGlobals;
import com.byteshaft.foodtruck.utils.Helpers;
import com.byteshaft.foodtruck.utils.SimpleDividerItemDecoration;
import com.byteshaft.requests.HttpRequest;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;


public class TruckList extends AppCompatActivity implements HttpRequest.OnReadyStateChangeListener,
        HttpRequest.OnErrorListener {

    public RecyclerView mRecyclerView;
    private CustomView viewHolder;
    private CustomAdapter mAdapter;
    private HttpRequest request;
    private ArrayList<TruckDetail> truckDetails;
    private TextView truckTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.owner_truck_list);
        truckDetails = new ArrayList<>();
        Log.i("TAG" , ""+ AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView = (RecyclerView) findViewById(R.id.owner_truck_list_recycler_view);
        truckTextView = (TextView) findViewById(R.id.no_truck_text_view);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.canScrollVertically(LinearLayoutManager.VERTICAL);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        mAdapter = new CustomAdapter(truckDetails, this);
        mRecyclerView.setAdapter(mAdapter);
        getTruckDetails();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_new_truck) {
            startActivity(new Intent(getApplicationContext(), AddNewTruck.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getTruckDetails() {
        request = new HttpRequest(getApplicationContext());
        request.setOnReadyStateChangeListener(this);
        request.setOnErrorListener(this);
        request.open("GET", String.format("%suser/trucks", AppGlobals.BASE_URL));
        request.setRequestHeader("Authorization", "Token " +
                AppGlobals.getStringFromSharedPreferences(AppGlobals.KEY_TOKEN));
        request.send();
        Helpers.showProgressDialog(TruckList.this, "Fetching truck details");
    }

    @Override
    public void onReadyStateChange(HttpRequest request, int readyState) {
        switch (readyState) {
            case HttpRequest.STATE_DONE:
                Helpers.dismissProgressDialog();
                switch (request.getStatus()) {
                    case HttpURLConnection.HTTP_OK:
                    Log.i("TAG", "Truck "+ request.getResponseText());
                        try {
                            JSONArray jsonArray = new JSONArray(request.getResponseText());
                            if (jsonArray.length() > 0) {

                            } else {
                                truckTextView.setVisibility(View.VISIBLE);
                                mRecyclerView.setVisibility(View.GONE);
                            }
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                TruckDetail truckDetail = new TruckDetail();
                                truckDetail.setId(jsonObject.getInt("id"));
                                truckDetail.setTruckName(jsonObject.getString("name"));
                                truckDetail.setAddress(jsonObject.getString("address"));
                                truckDetail.setLatLng(jsonObject.getString("location"));
                                truckDetail.setContactNumber(jsonObject.getString("phone_number"));
                                truckDetail.setProducts(jsonObject.getString("products"));
                                truckDetail.setImageUrl(Uri.parse(jsonObject.getString("photo")
                                        .replace("http://localhost/", AppGlobals.SERVER_IP)));
                                truckDetail.setRating(jsonObject.getString("ratings"));
                                truckDetail.setWebsiteUrl(jsonObject.getString("website"));
                                truckDetail.setFacebookUrl(jsonObject.getString("facebook"));
                                truckDetail.setInstagramUrl(jsonObject.getString("instagram"));
                                truckDetail.setTwitterUrl(jsonObject.getString("twitter"));
                                truckDetails.add(truckDetail);
                                mAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;

                }
        }
    }

    @Override
    public void onError(HttpRequest request, int readyState, short error, Exception exception) {
        Helpers.dismissProgressDialog();
    }

    class CustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
            RecyclerView.OnItemTouchListener {

        private ArrayList<TruckDetail> items;
        private OnItemClickListener mListener;
        private GestureDetector mGestureDetector;
        private Activity mActivity;

        public CustomAdapter(ArrayList<TruckDetail> truckDetails, Context context,
                             OnItemClickListener listener) {
            this.items = truckDetails;
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        public CustomAdapter(ArrayList<TruckDetail> truckDetails, Activity activity) {
            this.items = truckDetails;
            this.mActivity = activity;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.delegate_owner_truck, parent, false);
            viewHolder = new CustomView(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            holder.setIsRecyclable(false);
            TruckDetail truckDetail = items.get(position);
            viewHolder.truckName.setText(truckDetail.getTruckName());
            viewHolder.truckAddress.setText(truckDetail.getAddress());
            viewHolder.products.setText(truckDetail.getProducts());
            viewHolder.truckName.setTypeface(AppGlobals.typefaceNormal);
            viewHolder.products.setTypeface(AppGlobals.typefaceNormal);
            viewHolder.truckAddress.setTypeface(AppGlobals.typefaceNormal);
            Picasso.with(mActivity)
                    .load(truckDetail.getImageUrl())
                    .resize(150, 150)
                    .centerCrop()
                    .into(viewHolder.imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {


                        }
                    });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View childView = rv.findChildViewUnder(e.getX(), e.getY());
//            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
//                mListener.onItem(items.get(rv.getChildPosition(childView)), (TextView)
//                        rv.findViewHolderForAdapterPosition(rv.getChildPosition(childView)).
//                                itemView.findViewById(R.id.specific_category_title));
//                return true;
//            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public interface OnItemClickListener {
        void onItem(Integer item, TextView textView);
    }

    // custom viewHolder to access xml elements requires a view in constructor
    public static class CustomView extends RecyclerView.ViewHolder {
        public TextView truckName;
        public TextView products;
        public ImageView imageView;
        public TextView truckAddress;

        public CustomView(View itemView) {
            super(itemView);
            truckName = (TextView) itemView.findViewById(R.id.truck_name);
            products = (TextView) itemView.findViewById(R.id.products);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            truckAddress = (TextView) itemView.findViewById(R.id.truck_address);
        }
    }
}