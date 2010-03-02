package com.iolog.mongodbtools

import org.codehaus.groovy.grails.commons.GrailsClassUtils

/**
 * User: mark
 * Date: Feb 28, 2010
 * Time: 3:11:14 PM
 */
class MongoMapperModel {

   Class clazz
   MongoMapperField[] fields
   String typeName





   public MongoMapperModel(Class clazz, Map fieldNames)
   {
      this.clazz = clazz;
      setMappedFields( fieldNames )
   }

   /**
    * proccesses a map of mongoFieldName -> domainFieldName to populate the MongoMapperModel's
    * field definitions.  These are then used during the restore/save process
    * @param fieldNames
    */
   public void setMappedFields(Map fieldNames)
   {
      fields = new MongoMapperField[fieldNames.size()]
      def idx = 0
      fieldNames.each { alias,fieldName ->
         MongoMapperField mmf = new MongoMapperField();

         Class type = GrailsClassUtils.getPropertyType(this.clazz,fieldName)
         mmf.fieldType = type
         mmf.domainFieldName = fieldName
         mmf.mongoFieldName = alias

         fields[idx++] = mmf
      }
   }

}



