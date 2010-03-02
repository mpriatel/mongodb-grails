package com.iolog.mongodbtools

/**
 * User: mark
 * Date: Mar 1, 2010
 * Time: 12:23:44 AM
 */
class MongoMapperField
{
   String domainFieldName
   String mongoFieldName
   Class fieldType

   /**
    * flag which indicates that this field's class type has a registered mapper.
    */
   boolean isMapped


   public String toString()
   {
      return "MongoMapperField{" +
         "mongoFieldName='" + mongoFieldName + '\'' +
         ", domainFieldName='" + domainFieldName + '\'' +
         ", fieldType=" + fieldType +
         '}';
   }
}