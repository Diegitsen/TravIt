package com.example.diego.mytwitter

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.view.View
import android.widget.SimpleAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_login.*
import java.io.ByteArrayOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class Login : AppCompatActivity() {

    private var mAuth:FirebaseAuth? = null

    private var database = FirebaseDatabase.getInstance()
    private var myRef=database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()

      ivProfilePic.setOnClickListener(View.OnClickListener {
           checkPermission()
        })
    }

    val READIMAGE:Int = 132
    fun checkPermission()
    {
        if(Build.VERSION.SDK_INT>=23)
        {
            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), READIMAGE)
                return
            }

           loadImage()
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array< String>, grantResults: IntArray) {
        when(requestCode)
        {
            READIMAGE->
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    loadImage()
                   // Toast.makeText(applicationContext, "good", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(applicationContext, "No se puede acceder a la memeoria", Toast.LENGTH_SHORT).show()


            else ->super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }



    fun eLogin(view:View)
    {
        loginToFirebase(etEmail.text.toString(), etPassword.text.toString())
    }

    fun loginToFirebase(email:String, password:String)
    {
        mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){
            task->if(task.isSuccessful)
        {
            /*cuidaooo*/Toast.makeText(applicationContext,"exito", Toast.LENGTH_SHORT).show()

            saveImageInFirebase()
        }
        else
            Toast.makeText(this,"error", Toast.LENGTH_SHORT).show()
        }
    }


    fun saveImageInFirebase()
    {
        var currentUser = mAuth!!.currentUser
        val email:String = currentUser!!.email.toString()
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReferenceFromUrl("gs://mytwitter-e4f08.appspot.com/")
        val df = SimpleDateFormat("ddMMyyHHmmss")
        val dataobj = Date()
        val imagePath = splitString(email) + "." + df.format(dataobj) + ".jpg"
        //esto guarda en la subcarpeta images, la ruta , por asi decrilo, de la imagen subida
        val imageRef=storageRef.child("images/" + imagePath)
        ivProfilePic.isDrawingCacheEnabled = true
        ivProfilePic.buildDrawingCache()

        //proceso para convertir imagen a bitmap
        val drawable=ivProfilePic.drawable as BitmapDrawable
        val bitmap=drawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data = baos.toByteArray()
        val uploadTask=imageRef.putBytes(data)
        uploadTask.addOnFailureListener()//////////////
        {
            Toast.makeText(applicationContext, "fallo al subir", Toast.LENGTH_SHORT).show()
        }.addOnSuccessListener {
            taskSnapshot ->var downloadURL = taskSnapshot.downloadUrl!!.toString()

            myRef.child("Users").child(currentUser.uid).child("email").setValue(currentUser.email)
            myRef.child("Users").child(currentUser.uid).child("ProfileImage").setValue(downloadURL)///
            loadTweets()
        }
    }

    fun splitString(email:String):String
    {
        val split = email.split("@")
        return split[0]
    }

    override fun onStart() {
        super.onStart()
        loadTweets()
    }

    fun loadTweets()
    {
        var currentUser = mAuth!!.currentUser

        if(currentUser!=null)
        {
            var intent = Intent(this, MainActivity::class.java)

            intent.putExtra("email", currentUser.email)
            intent.putExtra("uid", currentUser.uid)

            startActivity(intent)
        }




    }

    //Proceso para subir foto a la app

    val PICK_IMAGE_CODE= 123

    fun loadImage()
    {
        var intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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
            ivProfilePic.setImageBitmap(BitmapFactory.decodeFile(picturePath))
        }
    }
}
