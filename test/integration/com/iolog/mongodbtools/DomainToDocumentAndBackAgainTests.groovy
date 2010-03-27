package com.iolog.mongodbtools

import grails.test.*
import com.acme.*
import com.mongodb.*

class DomainToDocumentAndBackAgainTests extends GrailsUnitTestCase {
    
    def mongo   
    def collection
    def date
    def widget
    
    protected void setUp() {
        super.setUp()
        collection = mongo.server1.test.integrationtests
        
        collection.drop()
        
        date = new GregorianCalendar(2010, Calendar.JANUARY, 21, 11, 22, 56).time
        widget = new MappedWidget(
            stockCount: 20, 
            length: 10.10, 
            description: 'A Mapped Widget', 
            createDate: date, 
            active: true,
            stockNumbers: [1,2,3,4,5,6,7,8,9])
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testDomainObjectToMongoDoc() {
        def mongoDoc = widget.toMongoDoc()
        assertEquals 20, mongoDoc.sc
        assertEquals 10.10, mongoDoc.l
        assertEquals 'A Mapped Widget', mongoDoc.d 
        assertEquals date, mongoDoc.cd
        assertEquals true, mongoDoc.a
        assertEquals ([1,2,3,4,5,6,7,8,9], mongoDoc.sn)
    }
    
    void testMongoDocToDomainObject() {
        collection.save( widget.toMongoDoc() ) 
        def cursor = collection.find()
        assertEquals 1, cursor.count()
        def mongoWidget = cursor.next().toObject()
        
        assertEquals 20, mongoWidget.stockCount
        assertEquals 10.10, mongoWidget.length
        assertEquals 'A Mapped Widget', mongoWidget.description 
        assertEquals date, mongoWidget.createDate
        assertEquals true, mongoWidget.active
        assertEquals ([1,2,3,4,5,6,7,8,9], mongoWidget.stockNumbers)
        assertNotNull mongoWidget._id
    }
}
