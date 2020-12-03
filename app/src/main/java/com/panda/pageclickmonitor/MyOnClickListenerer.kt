package com.panda.pageclickmonitor

import android.util.Log
import android.view.View

class MyOnClickListenerer(var onClickListener: View.OnClickListener?) : View.OnClickListener {

    override fun onClick(v: View?) {
        Log.e("lz", "点击了一个按钮——$v")
        onClickListener!!.onClick(v)
    }
}