package edu.usf.cutr.opentripplanner.android.util;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.usf.cutr.opentripplanner.android.model.AddressModel;
import edu.usf.cutr.opentripplanner.android.model.Server;
import edu.usf.cutr.opentripplanner.android.sqlite.RecentAddressDb;
import edu.usf.cutr.opentripplanner.android.util.CustomAddress;

public class PlacesAutoCompleteAdapter extends ArrayAdapter<CustomAddress> implements Filterable {

    private Context context;
    private Server selectedServer;

    private ArrayList<CustomAddress> resultList = new ArrayList<CustomAddress>();
    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId,
                                     Server selectedServer) {
        super(context, textViewResourceId);
        this.context = context;
        this.selectedServer = selectedServer;
    }

    @Override
    public int getCount() {
        if (resultList != null){
            return resultList.size();
        }
        else{
            return 0;
        }
    }

    @Override
    public CustomAddress getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected Filter.FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null && constraint.length() > 0) {
                    // Retrieve the autocomplete results.
                        resultList = LocationUtil.processGeocoding(context, selectedServer,
                                constraint.toString());
                }else if (constraint != null && constraint.length() == 0){
                    //Retrieve recently used addresses
                    List<AddressModel> addreses = RecentAddressDb.getInstance(context).getAllAddresses();
                    if(addreses != null){
                        resultList.clear();
                        Locale locale = context.getResources().getConfiguration().locale;
                        for(AddressModel a : addreses){
                            resultList.add(a.getAsCustomAddress(locale));
                        }
                    }
                }
                if (resultList != null){
                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    public Server getSelectedServer() {
        return selectedServer;
    }

    public void setSelectedServer(Server selectedServer) {
        this.selectedServer = selectedServer;
    }

}