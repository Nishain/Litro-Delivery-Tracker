
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation


class CollapseAnimation(view: View) : Animation() {
    var mFromWidth: Int
    var mFromHeight: Int
    var mView: View
    var phase = 1
    override fun applyTransformation(
        interpolatedTime: Float,
        t: Transformation?
    ) {
        if(phase==1)
            mView.translationX = mFromWidth * interpolatedTime
        else{
            val newHeight: Int
            newHeight = (mFromHeight * (1 - interpolatedTime)).toInt()
            mView.getLayoutParams().height = newHeight
        }
        mView.requestLayout()
    }
    override fun willChangeBounds(): Boolean {
        return true
    }

    init {
        mView = view
        mFromHeight = view.getHeight()
        mFromWidth = view.width
    }
}