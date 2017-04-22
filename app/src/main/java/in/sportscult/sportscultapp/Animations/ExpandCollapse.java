package in.sportscult.sportscultapp.Animations;

import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import in.sportscult.sportscultapp.R;
import in.sportscult.sportscultapp.Utils.ExpandAndCollapseViewUtil;

/**
 * Created by Vikas on 11-04-2017.
 */

public class ExpandCollapse {

    private static final int DURATION = 250;
    private ImageView imageView;
    private ViewGroup view;


    public ExpandCollapse(){
        //required empty constructor
    }

    /**
     *
     * @param viewGroup the layout which needs to be expanded or collapsed
     * @param imageView the imageview which indicates whether the view is in exapnded or collapsed state
     */
    public ExpandCollapse(ViewGroup viewGroup, ImageView imageView) {
        this.view = viewGroup;

        this.imageView = imageView;

        if (view.getVisibility() == View.GONE) {
            ExpandAndCollapseViewUtil.expand( view, DURATION);
            imageView.setImageResource(R.drawable.ic_expand_more_black_24dp);
            rotate(-180.0f);
        } else {
            ExpandAndCollapseViewUtil.collapse( view, DURATION);
            imageView.setImageResource(R.drawable.ic_expand_less_black_24dp);
            rotate(180.0f);
        }
    }
    /**
     * Animates the arrow button in Request a call card in HelpFragment
     * @param angle
     */

    private void rotate(float angle) {
        Animation animation = new RotateAnimation(0.0f, angle, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);
        animation.setDuration(DURATION);
        imageView.startAnimation(animation);
    }
}
