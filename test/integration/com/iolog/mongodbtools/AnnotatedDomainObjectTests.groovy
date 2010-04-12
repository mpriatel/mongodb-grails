package com.iolog.mongodbtools

import grails.test.*
import com.acme.*
import com.mongodb.*

class AnnotatedDomainObjectTests extends MongoDbTestCase {

    def annotatedWidget
    def date

    protected void setUp() {
        super.setUp()
    
        date = new GregorianCalendar(2001, Calendar.APRIL, 17, 10, 13, 01).time
    
        annotatedWidget = new AnnotatedWidget(
            stockCount: 50, 
            length: 11.11, 
            description: 'A New Widget with annotations', 
            createDate: date, 
            active: true,
            stockNumbers: [9,8,7,6,5,4,3,2,1])    
    }

    void testDomainObjectToMongoDocShouldContainAllDomainFieldsByAlias() {
        
        def doc = annotatedWidget.toMongoDoc()
        
        assertEquals 50, doc.sc
        assertEquals 11.11, doc.l
        assertEquals 'A New Widget with annotations', doc.d 
        assertEquals date, doc.cd
        assertEquals true, doc.a
        assertEquals ([9,8,7,6,5,4,3,2,1], doc.sn)
        assertNull doc._id        
        assertNull doc.unmappedField
    }
    
    void testMongoDocToDomainObjectShouldContainAllDocumentFieldsByName() {

        collection.save( annotatedWidget.toMongoDoc() ) 
        def cursor = collection.find()
        assertEquals 1, cursor.count()
        def widget = cursor.next().toObject()
        
        assertEquals 50, widget.stockCount
        assertEquals 11.11, widget.length
        assertEquals 'A New Widget with annotations', widget.description 
        assertEquals date, widget.createDate
        assertEquals true, widget.active
        assertEquals ([9,8,7,6,5,4,3,2,1], widget.stockNumbers)
        assertNotNull widget._id
    }

}
