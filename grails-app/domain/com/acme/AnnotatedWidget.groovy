package com.acme

import com.mongodb.ObjectId

import com.iolog.mongodbtools.*

@MongoCollection('annotatedWidget')
class AnnotatedWidget {

    @MongoId
    String _id
    
    @MongoField('sc')
    int stockCount
    
    @MongoField('l')
    double length
    
    @MongoField('d')
    String description
    
    @MongoField('cd')
    Date createDate
    
    @MongoField('a')
    boolean active
    
    @MongoField('sn')
    List stockNumbers
    
    @MongoField('f')
    Map features
    
}
