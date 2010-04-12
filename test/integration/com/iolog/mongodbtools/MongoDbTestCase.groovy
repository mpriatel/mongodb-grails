package com.iolog.mongodbtools

import grails.test.*
import com.mongodb.*

class MongoDbTestCase extends GrailsUnitTestCase {

    def mongo
    def collection

    protected void setUp() {
        super.setUp()
        try {
            collection = mongo.server1.test.integrationtests
            collection.drop()
        } catch (MongoException e) {
            throw new MongoException('Could not connect to MongoDB - are you sure it is running?', e)
        }
    }

}
