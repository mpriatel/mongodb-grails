package com.acme

import com.iolog.mongodbtools.*
import org.bson.types.ObjectId

@MongoCollection('annotatedWidget')
class AnnotatedWidget {

    @MongoId
    String _id
    
    @MongoField('sc')
    Integer stockCount
    
    @MongoField('l')
    Double length
    
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
