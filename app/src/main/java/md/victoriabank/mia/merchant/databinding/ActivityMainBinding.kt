package md.victoriabank.mia.merchant.databinding

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import md.victoriabank.mia.merchant.R

class ActivityMainBinding private constructor(
    val root: View,
    val fragmentContainer: FrameLayout,
    val bottomNavigation: BottomNavigationView
) {
    companion object {
        fun inflate(inflater: LayoutInflater): ActivityMainBinding {
            val root = inflater.inflate(R.layout.activity_main, null, false)
            return ActivityMainBinding(
                root,
                root.findViewById(R.id.fragment_container),
                root.findViewById(R.id.bottom_navigation)
            )
        }
    }
}
