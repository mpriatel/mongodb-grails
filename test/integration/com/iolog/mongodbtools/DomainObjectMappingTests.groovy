package com.iolog.mongodbtools

import grails.test.*
import com.acme.*
import com.mongodb.*

class DomainObjectMappingTests extends MongoDbTestCase {

  def mappedWidget
  def annotatedWidget
  def date

  protected void setUp() {
    super.setUp()

    date = new GregorianCalendar(2001, Calendar.APRIL, 17, 10, 13, 01).time

    def props = [
      stockCount: 50, 
      length: 11.11, 
      description: 'A New Widget', 
      createDate: date, 
      active: true,
      stockNumbers: [9,8,7,6,5,4,3,2,1],
      features: [a:1, b:'two', c:3.0]
    ]

    annotatedWidget = new AnnotatedWidget( props )
    mappedWidget = new MappedWidget( props )
  
  }

  void testDomainObjectToMongoDocShouldContainAllDomainFieldsByAlias() {
    
    verifyDoc annotatedWidget.toMongoDoc()
    verifyDoc mappedWidget.toMongoDoc()
    
  }
  
  void testMongoDocToDomainObjectShouldContainAllDocumentFieldsByName() {

    def widget
    widget = saveAndRetrieve( annotatedWidget )
    verifyDomain widget
    
    widget = saveAndRetrieve( mappedWidget )
    verifyDomain widget
  
  }
  
  def verifyDoc = { doc ->
  
    assertEquals 50, doc.sc
    assertEquals 11.11, doc.l
    assertEquals 'A New Widget', doc.d 
    assertEquals date, doc.cd
    assertEquals true, doc.a
    assertEquals ([9,8,7,6,5,4,3,2,1], doc.sn)
    assertEquals ([a:1, b:'two', c:3.0], doc.f)
    assertNull doc._id        
    assertNull doc.unmappedField
  
  }
  
  def saveAndRetrieve = { widget ->
  
    collection.drop()
    collection.save( widget.toMongoDoc() ) 
    def cursor = collection.find()
    assertEquals 1, cursor.count()
    return cursor.next().toObject()
  
  }
    
  def verifyDomain = { widget ->
  
    assertEquals 50, widget.stockCount
    assertEquals 11.11, widget.length
    assertEquals 'A New Widget', widget.description 
    assertEquals date, widget.createDate
    assertEquals true, widget.active
    assertEquals ([9,8,7,6,5,4,3,2,1], widget.stockNumbers)
    def features = widget.features
    assertEquals 1, features.a
    assertEquals 'two', features.b
    assertEquals 3.0, features.c
    assertNotNull widget._id
    
  }

}
