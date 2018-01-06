package com.example.diego.mytwitter

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_ticket.view.*

class MainActivity : AppCompatActivity() {

    var listTweets = ArrayList<Ticket>()
    var myAdapter:MyTweetAdpater?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //data xd
        listTweets.add(Ticket("0", "holis", "url", "add"))
        listTweets.add(Ticket("0", "holiwis", "url", "diego"))
        listTweets.add(Ticket("0", "holis", "url", "diego"))
        listTweets.add(Ticket("0", "holiwis", "url", "diego"))

        myAdapter = MyTweetAdpater(this,listTweets)
        lvTickets.adapter = myAdapter
    }


    inner class  MyTweetAdpater: BaseAdapter {
        var listNotesAdpater= java.util.ArrayList<Ticket>()
        var context: Context?=null
        constructor(context: Context, listNotesAdpater: java.util.ArrayList<Ticket>):super(){
            this.listNotesAdpater=listNotesAdpater
            this.context=context
        }

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {


            var mytweet=listNotesAdpater[p0]

            if(mytweet.tweetPersonUID.equals("add")) {
                var myView = layoutInflater.inflate(R.layout.add_ticket, null)

                return myView
            }
            else{
                var myView=layoutInflater.inflate(R.layout.tweet_ticket,null)

                return myView
            }
        }

        override fun getItem(p0: Int): Any {
            return listNotesAdpater[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getCount(): Int {

            return listNotesAdpater.size

        }

    }

}
