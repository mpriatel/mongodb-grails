package com.iolog.mongodbtools;

import com.mongodb.Mongo;


import org.springframework.beans.factory.InitializingBean

import com.mongodb.DBApiLayer

import com.mongodb.BasicDBObject
import com.mongodb.BasicDBList

/**
 * User: mark
 * Date: Feb 27, 2010
 * Time: 1:53:53 PM
 */
public class MongoDbWrapper implements InitializingBean
{
   def grailsApplication

   Map<String, Mongo> mongos = new HashMap<String, Mongo>();
   Map<String, Object> shortcuts = new HashMap<String, Object>();
   Map<String, Class> typeMappings = new HashMap<String, Class>()
   Map<String, MongoMapperModel> mappersByTypeName = new HashMap<String, MongoMapperModel>()
   Map<Class, MongoMapperModel> mappersByClass = new HashMap<Class, MongoMapperModel>()
   Map<Class,String> typeForClassMap = new HashMap<Class,String>()

   /**
    *
    * @return
    */
   public Map<String, MongoMapperModel> getMappers() { return mappersByTypeName }

   /**
    *
    */
   public MongoMapperModel getMapper(String typeName) { return mappersByTypeName.get(typeName) }




   /**
   *
    */
   public MongoMapperModel getMapperForClass(Class c){ return mappersByClass.get(c) }

   /**
   *
    */
   public boolean typeIsMapped( Class c ){ return mappersByTypeName.containsKey(c) }


    /**
     * given a Class, this method returns the typeName used to associate a Mongo record/document
     * with a class
     *
     */
   public String getTypeNameForClass( Class  ){ return typeForClassMap.get(c) }

   /**
    * add support so that in Groovy, databases can be referenced using
    * mongo.<database>
    * @param key
    * @return
    */
   def Object propertyMissing(String name)
   {
      if (mongos.containsKey(name)) return mongos.get(name);
      if (shortcuts.containsKey(name)) return shortcuts.get(name);

      // first check shortcuts config to see if the propery references
      // a DBCollection
      if (grailsApplication.config.mongo.shortcuts)
      {
         grailsApplication.config.mongo.shortcuts.each {
            println it
         }
      }

      // if no MongoDB Is registered for the given key, lookup its
      // connection details in grailsApplication.config
      def dbConfig = grailsApplication.config.mongo.databases?."${name}"
      if (!dbConfig)
      {
         throw new IllegalArgumentException("No MongoDB config found in grailsApplicaiton.config.mongo.databases.${name}")
      }


      def port = dbConfig.port ? dbConfig.port : 27017


      Mongo mongo = new Mongo(dbConfig.host, port)
      mongos.put(name, mongo)
      return mongo
   }

   /**
    * during the plugin initialization domain classes which have a static
    * 'mongoFields' property will have a MongoMapperModel created for them
    * which are then added to the wrapper.
    */
   public void addMapperModel(String typeName, MongoMapperModel mmm)
   {
      println mmm
      typeForClassMap.put( mmm.clazz , typeName )
      mappersByTypeName.put(typeName, mmm)
      mappersByClass.put(mmm.clazz,mmm)
   }

   /**
    * add Groovy helpers to MongoDB objects.
    */
   void afterPropertiesSet()
   {

      Mongo.metaClass.propertyMissing = { String name ->
         return ((Mongo) delegate).getDB(name)
      }

      DBApiLayer.metaClass.propertyMissing = { String name ->
         return ((DBApiLayer) delegate).getCollection(name)
      }

      BasicDBList.metaClass.toObject = {
         List oList = new ArrayList((int) delegate.size())
         delegate.each {
            if (it instanceof BasicDBObject || it instanceof BasicDBList) oList.add(it.toObject())
            else oList.add(it)
         }
      }

      BasicDBObject.metaClass.toObject = {
         BasicDBObject _self = (BasicDBObject) delegate

         def typeName = _self.get("_t")

         if (!typeName)
         {
            return delegate
         }

         MongoMapperModel mmm = mappersByTypeName.get(typeName)

         if (!mmm)
         {
            return delegate
         }

         // begin mapping process to create a new object using
         // information stored inthe MongoMapperModel

         def obj = mmm.clazz.newInstance()

         for (MongoMapperField mmf: mmm.fields)
         {

            switch (mmf.fieldType)
            {
               case String:
                  obj."${mmf.domainFieldName}" = _self.get(mmf.mongoFieldName)
                  break

               case List:
                  BasicDBList mongoList = (BasicDBList) _self.get(mmf.mongoFieldName)
                  if (!mongoList)
                  {
                     obj."${mmf.domainFieldName}" = new ArrayList()
                  }
                  else
                  {
                     obj."${mmf.domainFieldName}" = mongoList.toObject()
                  }

                  break

            }


         }

         return obj


      }

      grailsApplication.config.mongo.shortcuts.each { shortcutName, String path ->
         def pathParts = path.split("\\/")
         def obj = this."${pathParts[0]}"."${pathParts[1]}"."${pathParts[2]}"
         if (obj) shortcuts.put(shortcutName, obj)
      }

      grailsApplication.config.mongo.mappings.each { type, className ->
         Class c = grailsApplication.getClassForName(className)
         typeMappings.put(type, c)
      }

   }


}
