package com.djangoUnchained.ViberAPIOps

import com.djangoUnchained.ViberAPIOps.ViberSendMessage.SendMessage

/**
 * Created by Bhavya Jain.
 * 2023-01-20
 */
object Syntax {

  final implicit class ButtonText(val v: String) extends AnyVal {
    def buttonHTMLTag: String = "<b><font color=\"#E4EAEF\">" + v + "</font></b>"
    def recommend: String = "Recommend a " + v
  }
}
