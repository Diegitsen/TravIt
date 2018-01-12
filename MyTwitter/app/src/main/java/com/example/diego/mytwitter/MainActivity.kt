package com.example.diego.mytwitter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.google.firebase.auth.UserInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.add_ticket.view.*
import kotlinx.android.synthetic.main.tweet_ticket.view.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    var listTweets = ArrayList<Ticket>()
    var myAdapter:MyTweetAdpater?=null
    var myEmail:String?=null
    var downloadURL:String?=null
    var myUserUID:String?=null

    private var database = FirebaseDatabase.getInstance()
    private var myRef=database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var b:Bundle = intent.extras
        myEmail = b.getString("email")
        myUserUID = b.getString("uid")

        //data xd
        listTweets.add(Ticket("0", "holis", "url", "add"))


        myAdapter = MyTweetAdpater(this,listTweets)
        lvTickets.adapter = myAdapter

        loadPost()
    }

    fun uploadImage(bitmap:Bitmap)
    {
        listTweets.add(0, Ticket("0", "holis", "url", "loading"))
        myAdapter!!.notifyDataSetChanged()
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://mytwitter-e4f08.appspot.com/")
        val df = SimpleDateFormat("ddMMyyHHmmss")
        val dataobj = Date()
        val imagePath = splitString(myEmail!!) + "." + df.format(dataobj) + ".jpg"
        //esto guarda en la subcarpeta images, la ruta , por asi decrilo, de la imagen subida
        val imageRef=storageRef.child("imagePost/" + imagePath)


        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data = baos.toByteArray()
        val uploadTask=imageRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext, "fallo al subir", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
            taskSnapshot -> downloadURL = taskSnapshot.downloadUrl!!.toString()

            listTweets.removeAt(0   )
            myAdapter!!.notifyDataSetChanged()
        }

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


                myView.iv_attach.setOnClickListener(View.OnClickListener {
                 loadImage()
                })

                myView.iv_post.setOnClickListener(View.OnClickListener {
                   //myRef.child("posts").push().setValue(PostInfo(userUID = myUserUID!!, textPost = myView.etPost.text.toString(), image = downloadURL!!))
                    myRef.child("posts").push().setValue(PostInfo(myUserUID!!, myView.etPost.text.toString(), downloadURL!!))
                    /*myRef.child("posts").push().child("UserUID").setValue(myUserUID)
                    myRef.child("posts").push().child("text").setValue(myView.etPost.text.toString())
                    myRef.child("posts").push().child("postImage").setValue(downloadURL)*/

                    myView.etPost.setText("")
                })
               return myView
            } else if (mytweet.tweetPersonUID.equals("loading")){
                var myView = layoutInflater.inflate(R.layout.loading_ticket, null)
                return myView
            }
            else{
                var myView=layoutInflater.inflate(R.layout.tweet_ticket,null)
                myView.txt_tweet.setText(mytweet.tweetText)
                myView.txtUserName.setText(mytweet.tweetPersonUID)
                Picasso.with(context).load(mytweet.tweetImageURL).into(myView.tweet_picture)

                myRef.child("Users").child(mytweet.tweetPersonUID).addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(dataSnapshot: DataSnapshot?) {

                        try{
                            var td = dataSnapshot!!.value as HashMap<String, Any>

                            for(key in td.keys)
                            {
                                var userInfo = td[key] as String

                                if(key.equals("ProfileImage"))
                                {
                                    Picasso.with(context).load(userInfo).into(myView.picture_path)
                                }
                                else
                                {
                                    myView.txtUserName.setText(userInfo)
                                }


                            }
                        }catch (ex:Exception)
                        {

                        }

                    }

                    override fun onCancelled(p0: DatabaseError?) {

                    }
                })

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

    //load image

    val PICK_IMAGE_CODE= 123

    fun loadImage()
    {
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==PICK_IMAGE_CODE && data!=null/* && resultCode == Activity.RESULT_OK*/)
        {
            val selectedImage=data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = contentResolver.query(selectedImage,filePathColumn, null, null, null)
            cursor.moveToFirst()
            val columnIndex=cursor.getColumnIndex(filePathColumn[0])
            val picturePath=cursor.getString(columnIndex)
            cursor.close()
            uploadImage(BitmapFactory.decodeFile(picturePath))
        }
    }





    fun splitString(email:String):String
    {
        val split = email.split("@")
        return split[0]
    }

    /*fun loadPost()
    {
        myRef.child("posts")
                .addValueEventListener(object :ValueEventListener{

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {

                        try {

                            listTweets.clear()
                            listTweets.add(Ticket("0","him","url","add"))

                            var td= dataSnapshot!!.value as HashMap<String,Any>

                            for(key in td.keys){

                                var post= td[key] as HashMap<String,Any>

                                listTweets.add(Ticket(key,

                                        post["text"] as String,
                                        post["postImage"] as String
                                        ,post["userUID"] as String))


                            }


                            myAdapter!!.notifyDataSetChanged()
                        }catch (ex:Exception){}


                    }

                    override fun onCancelled(p0: DatabaseError?) {

                    }
                })
    }*/


    fun loadPost()
    {
        listTweets.clear()
        listTweets.add(Ticket("0", "holis", "url", "add"))

        myRef.child("posts").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot?) {

                try{
                    var td = dataSnapshot!!.value as HashMap<String, Any>

                    for(key in td.keys)
                    {
                        var post = td[key] as HashMap<String, Any>

                        listTweets.add(Ticket(key,
                                post["textPost"] as String,
                                post["image"] as String,
                                post["userUID"] as String))

                    }
                    myAdapter!!.notifyDataSetChanged()
                }catch (ex:Exception)
                {

                }

            }

            override fun onCancelled(p0: DatabaseError?) {

            }
        })
    }

}
