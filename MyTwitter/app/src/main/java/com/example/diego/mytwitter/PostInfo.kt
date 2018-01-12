package com.example.diego.mytwitter

/**
 * Created by diego on 7/01/18.
 */
class PostInfo
{
    var userUID:String?=null
    var textPost:String?=null
    var image:String?=null

    constructor(userUID:String, textPost:String, image:String)
    {
        this.userUID = userUID
        this.textPost = textPost
        this.image = image
    }

}