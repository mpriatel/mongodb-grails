package com.iolog.mongodbtools

import grails.test.*
import com.acme.*
import com.mongodb.*

class DomainToDocumentAndBackAgainTests extends GrailsUnitTestCase {
    
    def mongo   
    def collection
    def date
    def mappedWidget
    def annotatedWidget
    
    protected void setUp() {
        super.setUp()
        collection = mongo.server1.test.integrationtests
        
        collection.drop()
        
        date = new GregorianCalendar(2010, Calendar.JANUARY, 21, 11, 22, 56).time
        
        mappedWidget = new MappedWidget(
            stockCount: 20, 
            length: 10.10, 
            description: 'A New Widget', 
            createDate: date, 
            active: true,
            stockNumbers: [1,2,3,4,5,6,7,8,9])
            
        annotatedWidget = new AnnotatedWidget(
            stockCount: 20, 
            length: 10.10, 
            description: 'A New Widget', 
            createDate: date, 
            active: true,
            stockNumbers: [1,2,3,4,5,6,7,8,9])
        
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testDomainObjectToMongoDoc() {
        verifyMongoDoc mappedWidget.toMongoDoc()
        verifyMongoDoc annotatedWidget.toMongoDoc()        
    }
    
    private void verifyMongoDoc(doc) {
        assertEquals 20, doc.sc
        assertEquals 10.10, doc.l
        assertEquals 'A New Widget', doc.d 
        assertEquals date, doc.cd
        assertEquals true, doc.a
        assertEquals ([1,2,3,4,5,6,7,8,9], doc.sn)
    }
    
    void testMongoDocToDomainObject() {
        collection.save( mappedWidget.toMongoDoc() ) 
        def cursor = collection.find()
        assertEquals 1, cursor.count()
        def mongoWidget = cursor.next().toObject()
        
        verifyDomainObject mongoWidget
    }
    
    private void verifyDomainObject(widget) {
        assertEquals 20, widget.stockCount
        assertEquals 10.10, widget.length
        assertEquals 'A New Widget', widget.description 
        assertEquals date, widget.createDate
        assertEquals true, widget.active
        assertEquals ([1,2,3,4,5,6,7,8,9], widget.stockNumbers)
        assertNotNull widget._id
    }
    
}
