package io.ona.rdt.viewholder;

import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by samuelgithengi on 4/8/19.
 */

public class FooterViewHolder extends RecyclerView.ViewHolder {
    public TextView pageInfoView;
    public Button nextPageView;
    public Button previousPageView;

    public FooterViewHolder(android.view.View view) {
        super(view);
        this.nextPageView = view.findViewById(org.smartregister.R.id.btn_next_page);
        this.previousPageView = view.findViewById(org.smartregister.R.id.btn_previous_page);
        this.pageInfoView = view.findViewById(org.smartregister.R.id.txt_page_info);
    }
}
