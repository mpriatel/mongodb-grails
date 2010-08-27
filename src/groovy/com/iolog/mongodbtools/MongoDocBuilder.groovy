package com.iolog.mongodbtools

/**
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

import com.mongodb.BasicDBObject

/**
 * @author Mark Priatel <mark@iolog.com>
 */
class MongoDocBuilder extends BuilderSupport {

    BasicDBObject root
    MongoDbWrapper mongo




    def MongoDocBuilder(mongo) {
        this.mongo = mongo;
        this.root = new BasicDBObject()
        setCurrent(root)
    }



    protected void setParent(Object o, Object o1) { }

    /**
     * handles the builder syntax where a property contains a child document:
     *
     * property{*}*
     */
    protected Object createNode(Object o) {
        if (root == null) {
            root = new BasicDBObject()
            return root
        }
        def doc = new BasicDBObject()
        getCurrent().putAt(o, doc)
        return doc
    }

    /**
     * handles the builder syntax:
     * property("value")
     *
     * If the value has an associated mapper, it will first be converted into
     * the appropriate Mongo document.
     */
    protected Object createNode(Object o, Object o1) {
        def mapper = mongo.getMapperForClass(o1.getClass())
        if (mapper) {
            getCurrent().put(o, mapper.buildMongoDoc(o1))
        }
        else {
            getCurrent().put(o, o1)
        }

        // in this scenario we aren't creating any new objects,
        // just setting properties on the parent
        // ----------------------------------------------------
        return null;
    }

    /**
     * supports the builder syntax:
     * property( name:'mark', city:'ottawa' )
     *
     * todo: inspect map properties for mappable object types
     */
    protected Object createNode(Object o, Map map) {
        getCurrent().put(o, new BasicDBObject(map))
        return null
    }

    protected Object createNode(Object o, Map map, Object o1) {
        return null;
    }



    public static void main(String[] args) {
        def builder = new MongoDocBuilder(new MongoDbWrapper())

        def action = {
            '$inc' {
                name("FOO")
            }
        }

        action.setDelegate(builder)
        action.call()
        println builder.root

//      println  builder.test{
//         id(10)
//         username('mark')
//         password('pw')
//         nested( alive:true, times:40.4)
//         address{
//            postalCode("L5H 3Z7")
//            street("42 Huntview")
//            settings{
//               accept("FALSE")
//            }
//            another("LINE")
//         }
//
//      }
//
//      println builder.text{
//         name("BLASH")
//      }
    }

}
