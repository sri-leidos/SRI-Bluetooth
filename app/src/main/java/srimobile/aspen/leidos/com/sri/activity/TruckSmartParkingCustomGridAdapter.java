package srimobile.aspen.leidos.com.sri.activity;

import srimobile.aspen.leidos.com.sri.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import de.akquinet.android.androlog.Log;

/**
 * Created by walswortht on 5/26/2015.
 */
public class TruckSmartParkingCustomGridAdapter extends BaseAdapter{

    Context context;
    ArrayList<TruckSmartParking.TruckStopDetailsVo> gridItemsAL;
    TruckSmartParkingCustomGridAdapter customGridAdapter;

    private static LayoutInflater inflater=null;

    public TruckSmartParkingCustomGridAdapter(TruckSmartParking mainActivity, ArrayList<TruckSmartParking.TruckStopDetailsVo> gridItems) {

        context             =   mainActivity;
        this.gridItemsAL    =   gridItems;
        Log.init(mainActivity);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return gridItemsAL.size();
    }

    @Override
    public Object getItem(int position) {

        return gridItemsAL.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView location_name;
        TextView name;
        TextView distance;
        ImageView truckStopImg;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            inflater = ( LayoutInflater )context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        View gridItem;

        if (convertView == null) {

            gridItem = new View(context);

            gridItem = inflater.inflate(R.layout.truck_parking_grid_item, null);

            try {

                Holder holder = new Holder();

                holder.name = (TextView) gridItem.findViewById(R.id.text1);
                holder.location_name = (TextView) gridItem.findViewById(R.id.text2);
                holder.distance = (TextView) gridItem.findViewById(R.id.text3);
                holder.truckStopImg = (ImageView) gridItem.findViewById(R.id.truckStopImg);

                String urlBitmap = gridItemsAL.get(position).getBitmapUrl();

//            String URL = "https://s3.amazonaws.com/tsps_prod/chains/thumb/12-pilot.jpg?1406587345";

                Bitmap image = null;
                Log.e("URL", urlBitmap);

                // get image
                if (urlBitmap.trim().length() != 0) {
                    // download bitmap image
                    holder.truckStopImg.setTag(urlBitmap);
                    AsyncTask task = new DownloadImageTask(holder.truckStopImg).execute();
                }


                holder.name.setText(gridItemsAL.get(position).getName());
                holder.location_name.setText(gridItemsAL.get(position).getLocation_text());
                holder.distance.setText(gridItemsAL.get(position).getDistance());

                LinearLayout truckParkingGridItem = (LinearLayout) gridItem.findViewById(R.id.text4);
                truckParkingGridItem.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent launchIntent = context.getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.tsps.hud");
                        context.startActivity(launchIntent);
                    }
                });
            } catch (Exception e) {
                Log.e("LOG", "Doing on Start");

            }
        }else {
            gridItem = (View)convertView;
        }

        return gridItem;
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageTask(ImageView image) {
            this.imageView = image;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            String url = ((String)imageView.getTag() );
            Bitmap bm = download_Image((String)imageView.getTag());

            Log.e("URL", url);

            return bm;
        }

        protected void onPostExecute(Bitmap result) {

            imageView.setImageBitmap(result);

        }

        private Bitmap download_Image(String url) {

            Bitmap bm = null;
            try {
                if ( Patterns.WEB_URL.matcher(url).matches() ) {

                    URL aURL = new URL(url);
                    URLConnection conn = aURL.openConnection();
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    bm = BitmapFactory.decodeStream(bis);
                    bis.close();
                    is.close();
                }
// else {
//
////                    Uri path = Uri.parse("android.resource://" + context.getApplicationContext().getPackageName() + "/" + R.drawable.truck_photo_placeholder_grayed);
//
//                    bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.truck_photo_placeholder_grayed);
////                    bm = loadBitmap(path.toString());
//                }

            } catch (IOException e) {
                Log.e("LOG","Error getting the image from server : " + e.getMessage().toString());

            }
            return bm;

        }
        public Bitmap loadBitmap(String url)
        {
            Bitmap bm = null;
            InputStream is = null;
            BufferedInputStream bis = null;
            try
            {



                URLConnection conn = new URL(url).openConnection();
                conn.connect();
                is = conn.getInputStream();
                bis = new BufferedInputStream(is, 8192);
                bm = BitmapFactory.decodeStream(bis);
            }
            catch (Exception e)
            {
                Log.e("LOG","BITMAP : " + e.getMessage().toString());
            }
            finally {
                if (bis != null)
                {
                    try
                    {
                        bis.close();
                    }
                    catch (IOException e)
                    {
                        Log.e("LOG","BITMAP : " + e.getMessage().toString());
                    }
                }
                if (is != null)
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException e)
                    {
                        Log.e("LOG","BITMAP : " + e.getMessage().toString());
                    }
                }
            }
            return bm;
        }

    }

}
