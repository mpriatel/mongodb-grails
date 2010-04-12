package com.iolog.mongodbtools

import grails.test.*
import com.acme.*
import com.mongodb.*

class MappedDomainObjectTests extends MongoDbTestCase {

    def mappedWidget
    def date

    protected void setUp() {
        super.setUp()
    
        date = new GregorianCalendar(2010, Calendar.JANUARY, 21, 11, 22, 56).time
    
        mappedWidget = new MappedWidget(
            stockCount: 20, 
            length: 10.10, 
            description: 'A New Widget', 
            createDate: date, 
            active: true,
            stockNumbers: [1,2,3,4,5,6,7,8,9])    
    }

    void testDomainObjectToMongoDocShouldContainAllDomainFieldsByAlias() {
        
        def doc = mappedWidget.toMongoDoc()
        
        assertEquals 20, doc.sc
        assertEquals 10.10, doc.l
        assertEquals 'A New Widget', doc.d 
        assertEquals date, doc.cd
        assertEquals true, doc.a
        assertEquals ([1,2,3,4,5,6,7,8,9], doc.sn)
        assertNull doc._id        
        assertNull doc.unmappedField
    }
    
    void testMongoDocToDomainObjectShouldContainAllDocumentFieldsByName() {

        collection.save( mappedWidget.toMongoDoc() ) 
        def cursor = collection.find()
        assertEquals 1, cursor.count()
        def widget = cursor.next().toObject()
        
        assertEquals 20, widget.stockCount
        assertEquals 10.10, widget.length
        assertEquals 'A New Widget', widget.description 
        assertEquals date, widget.createDate
        assertEquals true, widget.active
        assertEquals ([1,2,3,4,5,6,7,8,9], widget.stockNumbers)
        assertNotNull widget._id
    }

}
