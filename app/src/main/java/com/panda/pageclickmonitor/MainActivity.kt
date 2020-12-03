package com.panda.pageclickmonitor


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.reflect.Field
import java.lang.reflect.Method


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //按钮1，普通点击事件
        btn1.setOnClickListener {
            showToast("点了按钮1")
        }

        //按钮2，打开一个dialog，dialog中有按钮点击事件
        btn2.setOnClickListener {
            val builder =
                AlertDialog.Builder(this)
                    .setTitle("我是一个dialog")
            val view: View = layoutInflater.inflate(R.layout.dialog_btn, null)
            val btn4 =
                view.findViewById<View>(R.id.btn4)
            btn4.setOnClickListener {
                showToast("点击了Dialog按钮")
            }

            builder.setView(view)
            var dialog=builder.create()
            dialog.show()

//            var rootView = dialog.window?.decorView as ViewGroup
//            hookAllChildView(rootView)
        }

        //按钮3，新增一个view，新增view中有按钮点击事件
        btn3.setOnClickListener {

            var button = Button(this)
            button.text = "我是新加的按钮"
            var param = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            mainlayout.addView(button, param)

            button.setOnClickListener {
                showToast("点击了新加的按钮")
            }
        }


        //开启无障碍服务
        btn6.setOnClickListener {
            startAccessibility()
        }

    }

    /**
     * 开启无障碍服务
     */
    private fun startAccessibility(){
        //去设置页开启无障碍服务
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    var lastX = 0f
    var lastY = 0f

    /**
     * 重写dispatchTouchEvent获取触摸事件
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            val x = it.x
            val y = it.y
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.e(Companion.TAG, "ACTION_DOWN")
                    lastX = x
                    lastY = y
                }

                MotionEvent.ACTION_MOVE -> {
//                    val offsetX = x - lastX
//                    val offsetY = y - lastY
//                    Log.e(Companion.TAG,"$offsetX--$offsetY")
                }

                MotionEvent.ACTION_UP -> {
                    Log.e(Companion.TAG, "ACTION_UP——CLICK")
                }
                else -> {
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }



    override fun onResume() {
        super.onResume()

//        var rootView = window.decorView as ViewGroup
//        hookAllChildView(rootView)
//
//        rootView.viewTreeObserver.addOnGlobalLayoutListener { hookAllChildView(rootView) }
    }

    /**
     * 遍历viewgroup下的所有view
     */
    private fun hookAllChildView(viewGroup: ViewGroup) {
        val count = viewGroup.childCount
        for (i in 0 until count) {
            if (viewGroup.getChildAt(i) is ViewGroup) {
                hookAllChildView(viewGroup.getChildAt(i) as ViewGroup)
            } else {
                hook(viewGroup.getChildAt(i))
            }
        }
    }

    /**
     * hook mOnClickListener
     */
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private fun hook(view: View) {
        try {
            val getListenerInfo: Method = View::class.java.getDeclaredMethod("getListenerInfo")
            getListenerInfo.isAccessible = true
            //获取当前View的ListenerInfo对象
            val mListenerInfo: Any = getListenerInfo.invoke(view)
            try {
                val listenerInfoClazz =
                    Class.forName("android.view.View\$ListenerInfo")
                try {
                    //获取mOnClickListener参数
                    val mOnClickListener: Field =
                        listenerInfoClazz.getDeclaredField("mOnClickListener")
                    mOnClickListener.isAccessible = true
                    var oldListener: View.OnClickListener? =
                        mOnClickListener.get(mListenerInfo) as? View.OnClickListener
                    if (oldListener != null && oldListener !is MyOnClickListenerer) {
                        //替换OnClickListenerer
                        val proxyOnClick =
                            MyOnClickListenerer(oldListener)
                        mOnClickListener.set(mListenerInfo, proxyOnClick)
                    }
                } catch (e: NoSuchFieldException) {
                    e.printStackTrace()
                }
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }
    }


    companion object {
        private const val TAG = "MainActivity"
    }
}